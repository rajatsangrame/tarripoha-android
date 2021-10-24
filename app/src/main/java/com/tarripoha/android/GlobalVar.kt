package com.tarripoha.android

object GlobalVar {

    private val languages = arrayListOf<String>()

    fun loadLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages
}