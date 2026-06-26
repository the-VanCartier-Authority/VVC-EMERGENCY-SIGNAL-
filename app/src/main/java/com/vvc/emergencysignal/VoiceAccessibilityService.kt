package com.vvc.emergencysignal

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.accessibility.AccessibilityEvent
import java.util.Locale

class VoiceAccessibilityService : AccessibilityService(), RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val codeWord = "código alfa"

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
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.get(0)?.lowercase(Locale.getDefault()) ?: ""
        
        if (text.contains(codeWord)) {
            val emergencyIntent = Intent(this, EmergencyService::class.java)
            startService(emergencyIntent)
        }
    }

    override fun onInterrupt() { shutdownRecognizer() }
    override fun onDestroy() { shutdownRecognizer(); super.onDestroy() }

    private fun shutdownRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onError(error: Int) {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
