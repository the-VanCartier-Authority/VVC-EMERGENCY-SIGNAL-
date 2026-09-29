package com.vvc.emergencysignal

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class EmergencyService : Service() {
    enum class State { IDLE, STARTING, ACQUIRING_LOCATION, SENDING_SMS, SIGNALING, STOPPING, FAILED }

    @Volatile private var state = State.IDLE
    private lateinit var cameraManager: CameraManager
    private lateinit var vibrator: Vibrator
    private lateinit var locationManager: LocationManager
    private var cameraId: String? = null
    private var signalThread: Thread? = null
    private val running = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        cameraId = runCatching { cameraManager.cameraIdList.firstOrNull() }.getOrNull()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopEmergency() else startEmergency()
        return START_NOT_STICKY
    }

    private fun startEmergency() {
        if (running.getAndSet(true)) return
        state = State.STARTING

        try {
            startForeground(
                NOTIFICATION_ID,
                buildNotification("Señal de emergencia activa"),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                else 0
            )
        } catch (_: Exception) {
            fail()
            return
        }

        thread(name = "emergency-dispatch") {
            val location = if (hasLocationPermission()) acquireCurrentLocation(8_000L) else null
            state = State.SENDING_SMS
            sendEmergencySms(location)
            state = State.SIGNALING
            startSignalLoop()
        }
    }

    private fun acquireCurrentLocation(timeoutMs: Long): Location? {
        state = State.ACQUIRING_LOCATION
        val providers = buildList {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) add(LocationManager.GPS_PROVIDER)
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) add(LocationManager.NETWORK_PROVIDER)
        }
        if (providers.isEmpty()) return null

        val lock = Object()
        var best: Location? = null
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (best == null || location.accuracy < best!!.accuracy) {
                    best = location
                    synchronized(lock) { lock.notifyAll() }
                }
            }
        }

        try {
            providers.forEach { provider ->
                locationManager.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
            }
            synchronized(lock) {
                val deadline = System.currentTimeMillis() + timeoutMs
                while (best == null && running.get() && System.currentTimeMillis() < deadline) {
                    lock.wait((deadline - System.currentTimeMillis()).coerceAtLeast(50L))
                }
            }
        } catch (_: SecurityException) {
            return null
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        } finally {
            runCatching { locationManager.removeUpdates(listener) }
        }
        return best
    }

    private fun sendEmergencySms(location: Location?) {
        val targetPhone = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getString("PREF_EMERGENCY_CONTACT", null)?.trim()
        if (targetPhone.isNullOrEmpty()) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return

        val coordinates = location?.let { "${it.latitude},${it.longitude}" }
        val message = coordinates?.let {
            "ALERTA DE EMERGENCIA VVC. Necesito ayuda urgente. Ubicación: https://www.google.com/maps/search/?api=1&query=$it"
        } ?: "ALERTA DE EMERGENCIA VVC. Necesito ayuda urgente. Ubicación no disponible."

        runCatching {
            val sms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) getSystemService(SmsManager::class.java)
            else @Suppress("DEPRECATION") SmsManager.getDefault()
            sms.sendTextMessage(targetPhone, null, message, null, null)
        }.onFailure { state = State.FAILED }
    }

    private fun startSignalLoop() {
        if (signalThread?.isAlive == true) return
        signalThread = thread(name = "sos-signal") {
            while (running.get()) {
                for (step in MorseSignalEngine.sosPattern()) {
                    if (!running.get()) break
                    try {
                        if (step.on) toggleHardware(true)
                        Thread.sleep(step.durationMs)
                        if (step.on) toggleHardware(false)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        return@thread
                    }
                }
            }
        }
    }

    private fun toggleHardware(enabled: Boolean) {
        runCatching {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraId?.let { cameraManager.setTorchMode(it, enabled) }
            }
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200L, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        }
    }

    private fun stopEmergency() {
        running.set(false)
        state = State.STOPPING
        signalThread?.interrupt()
        toggleHardware(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        state = State.IDLE
        stopSelf()
    }

    private fun fail() {
        running.set(false)
        state = State.FAILED
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "VVC Emergency Signal", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(text: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("VVC Emergency Signal")
            .setContentText(text)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

    override fun onDestroy() {
        running.set(false)
        signalThread?.interrupt()
        toggleHardware(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.vvc.emergencysignal.action.START"
        const val ACTION_STOP = "com.vvc.emergencysignal.action.STOP"
        private const val PREFS = "VVC_PREFS"
        private const val CHANNEL_ID = "emergency_signal"
        private const val NOTIFICATION_ID = 7001
    }
}
