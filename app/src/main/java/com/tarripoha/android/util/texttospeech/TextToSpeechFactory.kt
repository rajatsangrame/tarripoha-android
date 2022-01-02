package com.tarripoha.android.util.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import java.util.*

object TextToSpeechFactory {

    private var mapTextToSpeech: MutableMap<String, TextToSpeech> = mutableMapOf()

    fun get(context: Context, language: String): TextToSpeech {
        var tts = mapTextToSpeech[language]
        if (tts == null) {
            when (language) {
                GlobalVar.LANG_MAR, GlobalVar.LANG_HI, GlobalVar.LANG_EN -> {
                    tts = TextToSpeech(context) {
                        tts?.language = Locale.forLanguageTag(language)
                    }
                    mapTextToSpeech[language] = tts
                }
                else -> throw IllegalArgumentException(context.getString(R.string.error_language_not_supported))
            }
        }
        return tts
    }

    fun clear() {
        mapTextToSpeech.forEach {
            it.value.stop()
            it.value.shutdown()
        }
        mapTextToSpeech.clear()
    }
}