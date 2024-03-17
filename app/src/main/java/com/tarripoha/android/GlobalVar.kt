package com.tarripoha.android

object GlobalVar {


    private val languages = arrayListOf<String>()

    // Ref: https://jrgraphix.net/r/Unicode/0900-097F
    private val chars = arrayListOf<String>()

    fun setLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages

    fun setCharList(chars: List<String>) {
        this.chars.clear()
        this.chars.addAll(chars)
    }

    fun getCharList() = chars
}
