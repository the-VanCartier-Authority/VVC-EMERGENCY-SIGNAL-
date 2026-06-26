package com.vvc.emergencysignal

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.accessibility.AccessibilityEvent
import java.util.Locale

class VoiceAccessibilityService : AccessibilityService(), RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            startListeningOffline()
        }
    }

    private fun startListeningOffline() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
    }

    override fun onResults(results: Bundle?) {
        val sharedPrefs = getSharedPreferences("VVC_PREFS", Context.MODE_PRIVATE)
        val dynamicCodeWord = sharedPrefs.getString("PREF_VOICE_CODEWORD", "código alfa") ?: "código alfa"
        
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.get(0)?.lowercase(Locale.getDefault()) ?: ""
        
        if (text.contains(dynamicCodeWord.lowercase(Locale.getDefault()))) {
            val emergencyIntent = Intent(this, EmergencyService::class.java)
            startService(emergencyIntent)
        }
        startListeningOffline()
    }

    override fun onError(error: Int) {
        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
            shutdownRecognizer()
            handler.postDelayed({
                startListeningOffline()
            }, 500)
        }
    }

    override fun onInterrupt() { shutdownRecognizer() }
    override fun onDestroy() { 
        handler.removeCallbacksAndMessages(null)
        shutdownRecognizer()
        super.onDestroy() 
    }

    private fun shutdownRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
