package com.vvc.emergencysignal

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
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
            startEmergencyLoop()
        }
        return START_STICKY
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
        } catch (ignored: Exception) {}
    }

    override fun onDestroy() {
        isRunning = false
        toggleHardware(false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
