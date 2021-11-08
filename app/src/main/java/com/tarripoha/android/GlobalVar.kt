package com.tarripoha.android

object GlobalVar {

    const val TYPE_WORD = "word"
    const val TYPE_GOOGLE_AD = "google-ad"
    const val CATEGORY_MOST_VIEWED = "most-viewed"
    const val CATEGORY_TOP_LIKED = "top-liked"
    val DEBUG_MODE = BuildConfig.DEBUG
    private val languages = arrayListOf<String>()
    private val chars = arrayListOf(
        "अ", "आ", "इ", "ई", "ए", "ऐ", "ओ", "औ", "अं",
        "क", "ख", "ग", "घ",
        "च", "छ", "ज", "झ",
        "ट", "ठ", "ड", "ढ", "न",
        "त", "थ", "द", "ध", "ण",
        "य", "र", "ल", "व", "श",
        "ष", "स", "हा", "ळ", "क्ष", "त्र", "ज्ञ"
    )

    fun loadLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages

    fun getCharList() = chars
}
