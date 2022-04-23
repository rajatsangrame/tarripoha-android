package com.tarripoha.android.util.texttospeech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import com.tarripoha.android.util.TPUtils
import java.util.*

object TextToSpeechFactory {

    private const val TAG = "TextToSpeechFactory"
    private var mapTextToSpeech: MutableMap<String, TextToSpeech> = mutableMapOf()

    fun get(context: Context, language: String): TextToSpeech {
        var tts = mapTextToSpeech[language]
        if (tts == null) {
            when (language) {
                GlobalVar.LANG_MAR, GlobalVar.LANG_HI, GlobalVar.LANG_EN -> {
                    tts = TextToSpeech(context) {
                        when (it) {
                            TextToSpeech.SUCCESS -> {
                                tts?.language = Locale.forLanguageTag(language)
                                tts?.speak(null, TextToSpeech.QUEUE_FLUSH, null, null)
                                Log.i(TAG, "initialised")
                            }
                            TextToSpeech.LANG_NOT_SUPPORTED -> {
                                TPUtils.showToast(
                                    context = context,
                                    message = "Speech does not support the specified language on your phone"
                                )
                            }
                        }
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