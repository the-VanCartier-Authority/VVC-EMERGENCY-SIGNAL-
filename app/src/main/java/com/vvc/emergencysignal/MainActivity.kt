package com.vvc.emergencysignal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var isActivated = false
    private lateinit var etEmergencyContact: EditText
    private lateinit var etVoiceCodeWord: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etEmergencyContact = findViewById(R.id.etEmergencyContact)
        etVoiceCodeWord = findViewById(R.id.etVoiceCodeWord)
        loadPreferences()

        val listener = android.view.View.OnFocusChangeListener { _, focused -> if (!focused) savePreferences() }
        etEmergencyContact.onFocusChangeListener = listener
        etVoiceCodeWord.onFocusChangeListener = listener

        val triggerButton = findViewById<Button>(R.id.btnTrigger)
        triggerButton.setOnClickListener {
            savePreferences()
            if (!isAccessibilityServiceEnabled(this, VoiceAccessibilityService::class.java)) {
                Toast.makeText(this, "Activa el Servicio de Accesibilidad para el disparador por voz.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                return@setOnClickListener
            }
            val missing = requiredPermissions().filterNot(::hasPermission)
            if (missing.isNotEmpty()) {
                ActivityCompat.requestPermissions(this, missing.toTypedArray(), REQUEST_PERMISSIONS)
                return@setOnClickListener
            }
            toggleEmergencyService(triggerButton)
        }
    }

    private fun requiredPermissions(): List<String> = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.SEND_SMS)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun isAccessibilityServiceEnabled(context: Context, service: Class<*>): Boolean {
        val expected = android.content.ComponentName(context, service)
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            if (android.content.ComponentName.unflattenFromString(splitter.next()) == expected) return true
        }
        return false
    }

    private fun savePreferences() {
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("PREF_EMERGENCY_CONTACT", etEmergencyContact.text.toString().trim())
            .putString("PREF_VOICE_CODEWORD", etVoiceCodeWord.text.toString().trim())
            .apply()
    }

    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        etEmergencyContact.setText(prefs.getString("PREF_EMERGENCY_CONTACT", ""))
        etVoiceCodeWord.setText(prefs.getString("PREF_VOICE_CODEWORD", "código alfa"))
    }

    private fun toggleEmergencyService(button: Button) {
        if (!isActivated) {
            ContextCompat.startForegroundService(this, Intent(this, EmergencyService::class.java).setAction(EmergencyService.ACTION_START))
            button.text = "DETENER S.O.S."
            isActivated = true
        } else {
            stopService(Intent(this, EmergencyService::class.java).setAction(EmergencyService.ACTION_STOP))
            button.text = "SEÑAL S.O.S."
            isActivated = false
        }
    }

    companion object {
        private const val PREFS = "VVC_PREFS"
        private const val REQUEST_PERMISSIONS = 100
    }
}
