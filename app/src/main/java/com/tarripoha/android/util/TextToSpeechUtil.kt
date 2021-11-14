package com.tarripoha.android.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*


/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object TextToSpeechUtil {

    private const val TAG = "TextToSpeechUtil"

    private var textToSpeech: TextToSpeech? = null

    fun init(context: Context, lang: String, success: () -> Unit) {
        textToSpeech = TextToSpeech(context) {
            textToSpeech?.language = Locale.forLanguageTag(lang)
            success()
        }
    }

    fun speak(text: String) {
        if (textToSpeech == null) {
            Log.e(TAG, "speak: not initialised")
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun onStop() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }

}