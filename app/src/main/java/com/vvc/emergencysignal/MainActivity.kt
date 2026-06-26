package com.vvc.emergencysignal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var isActivated = false
    private val requiredPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECORD_AUDIO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnTrigger = findViewById<Button>(R.id.btnTrigger)
        btnTrigger.setOnClickListener {
            if (!isAccessibilityServiceEnabled(this, VoiceAccessibilityService::class.java)) {
                Toast.makeText(this, "Por favor, activa el Servicio de Accesibilidad", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            } else if (checkPermissions()) {
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

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expectedComponentName = android.content.ComponentName(context, service)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = android.content.ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return true
            }
        }
        return false
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
