package com.tarripoha.android

object GlobalVar {

    val DEBUG_MODE = BuildConfig.DEBUG
    private val languages = arrayListOf<String>()

    fun loadLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages
}
