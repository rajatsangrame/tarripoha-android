package com.tarripoha.android

object GlobalVar {

    const val TYPE_WORD = "word"
    const val TYPE_GOOGLE_AD = "google-ad"
    const val CATEGORY_MOST_VIEWED = "most-viewed"
    const val CATEGORY_TOP_LIKED = "top-liked"
    val DEBUG_MODE = BuildConfig.DEBUG
    private val languages = arrayListOf<String>()

    // Ref: https://jrgraphix.net/r/Unicode/0900-097F
    private val chars = arrayListOf(
        "अ", "आ", "इ", "ई", "उ", "ऊ", "ए", "ऐ", "ओ", "औ", "अं",
        "क", "ख", "ग", "घ", "ङ",
        "च", "छ", "ज", "झ", "ञ",
        "ट", "ठ", "ड", "ढ", "ण",
        "त", "थ", "द", "ध", "न",
        "प", "फ", "ब", "भ", "म",
        "य", "र", "ल", "व", "श",
        "ष", "स", "ह", "ळ", "क्ष", "त्र", "ॠ", "ज्ञ",
        "ा", "ि", "ी", "ु", "ू", "े", "ै", "ो", "ौ", "ं", "ः"
    )

    fun loadLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages

    fun getCharList() = chars
}
