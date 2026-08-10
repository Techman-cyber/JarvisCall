package com.jarvis.call

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

/**
 * The "voice" of Jarvis. Uses Android's built-in TextToSpeech —
 * completely free, works offline, no API key, no per-call cost.
 * Picks a male or female voice based on what the user chose in setup.
 */
class JarvisEngine(context: Context) {

    private var tts: TextToSpeech? = null
    private var ready = false
    private val prefs = context.getSharedPreferences("jarvis_prefs", Context.MODE_PRIVATE)

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                applyPreferredVoice()
                ready = true
            }
        }
    }

    private fun applyPreferredVoice() {
        val wantMale = prefs.getString("voice_gender", "female") == "male"
        val voices: Set<Voice>? = tts?.voices
        val match = voices?.firstOrNull { v ->
            val name = v.name.lowercase()
            if (wantMale) "male" in name && "female" !in name else "female" in name
        }
        if (match != null) {
            tts?.voice = match
        }
    }

    fun speak(text: String) {
        if (ready) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis_utterance")
        }
    }

    /** Very simple starting brain — swap this out for an LLM call later. */
    fun reply(heard: String): String {
        val ownerName = prefs.getString("owner_name", "the owner") ?: "the owner"
        val lower = heard.lowercase()
        return when {
            "who" in lower && "this" in lower -> "This is Jarvis, answering on behalf of $ownerName. They're unavailable right now."
            "call back" in lower || ("call" in lower && "later" in lower) -> "Got it, I'll let $ownerName know to call you back."
            "urgent" in lower || "emergency" in lower -> "Understood, this sounds urgent — I'm flagging this call as high priority for $ownerName right now."
            else -> "Thanks for calling. I've noted your message and will let $ownerName know."
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
