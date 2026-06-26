package com.vvc.emergencysignal

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var isActivated = false
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTrigger = findViewById<Button>(R.id.btnTrigger)
        btnTrigger.setOnClickListener {
            if (checkPermissions()) {
                toggleEmergencyService(btnTrigger)
            } else {
                ActivityCompat.requestPermissions(this, requiredPermissions, 100)
            }
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun toggleEmergencyService(button: Button) {
        val intent = Intent(this, EmergencyService::class.java)
        if (!isActivated) {
            startService(intent)
            button.text = "DETENER S.O.S."
            button.setBackgroundColor(android.graphics.Color.RED)
            isActivated = true
        } else {
            stopService(intent)
            button.text = "SEÑAL S.O.S."
            button.setBackgroundColor(android.graphics.Color.GREEN)
            isActivated = false
        }
    }
}
