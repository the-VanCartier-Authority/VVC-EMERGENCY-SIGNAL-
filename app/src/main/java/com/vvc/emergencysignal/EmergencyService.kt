package com.vvc.emergencysignal

import android.Manifest
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.location.Location
import android.location.LocationManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import kotlin.concurrent.thread

class EmergencyService : Service() {
    private var isRunning = false
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private lateinit var vibrator: Vibrator

    private val sosPattern = longArrayOf(
        0, 
        200, 200, 200, 200, 200, 200, // S
        400, 
        600, 200, 600, 200, 600, 200, // O
        400, 
        200, 200, 200, 200, 200, 200, // S
        2000 // Loop delay
    )

    override fun onCreate() {
        super.onCreate()
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            sendEmergencySMS()
            startEmergencyLoop()
        }
        return START_STICKY
    }

    private fun sendEmergencySMS() {
        try {
            val sharedPrefs = getSharedPreferences("VVC_PREFS", Context.MODE_PRIVATE)
            val targetPhone = sharedPrefs.getString("PREF_EMERGENCY_CONTACT", "")
            
            if (targetPhone.isNullOrEmpty()) {
                // Abort SMS dispatch but continue Morse loop
                return
            }

            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val lastLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val latitude = lastLocation?.latitude ?: 0.0
            val longitude = lastLocation?.longitude ?: 0.0

            val message = "¡ALERTA DE EMERGENCIA VVC! Necesito ayuda urgente. Mi ubicación actual: https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
            
            val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                this.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(targetPhone, null, message, null, null)
            
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startEmergencyLoop() {
        thread(start = true) {
            while (isRunning) {
                for (i in 1 until sosPattern.size step 2) {
                    if (!isRunning) break
                    try {
                        val duration = sosPattern[i]
                        toggleHardware(true)
                        Thread.sleep(duration)
                        
                        toggleHardware(false)
                        if (i + 1 < sosPattern.size) {
                            Thread.sleep(sosPattern[i + 1])
                        }
                    } catch (e: InterruptedException) { break }
                }
            }
        }
    }

    private fun toggleHardware(state: Boolean) {
        try {
            cameraId?.let { cameraManager.setTorchMode(it, state) }
            if (state) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (ignored: SecurityException) {
        } catch (ignored: Exception) {}
    }

    override fun onDestroy() {
        isRunning = false
        toggleHardware(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
