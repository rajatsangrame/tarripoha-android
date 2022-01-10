package com.tarripoha.android

object GlobalVar {

    const val TYPE_WORD = "word"
    const val TYPE_GOOGLE_AD = "google-ad"
    const val CATEGORY_MOST_VIEWED = "most-viewed"
    const val CATEGORY_TOP_LIKED = "top-liked"
    const val LANG_EN = "en"
    const val LANG_MAR = "mar"
    const val LANG_HI = "hi"
    val DEBUG_MODE = BuildConfig.DEBUG
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
