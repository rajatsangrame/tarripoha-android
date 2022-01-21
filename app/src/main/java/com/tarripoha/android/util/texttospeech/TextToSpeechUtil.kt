package com.tarripoha.android.util.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech


/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 */
object TextToSpeechUtil {

    fun init(context: Context, lang: String) {
        TextToSpeechFactory.get(context = context.applicationContext, language = lang)
    }

    fun speak(context: Context, lang: String, text: String) {
        val tts = TextToSpeechFactory.get(context = context.applicationContext, language = lang)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun onStop() {
        TextToSpeechFactory.clear()
    }

}