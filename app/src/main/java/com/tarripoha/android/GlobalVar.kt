package com.tarripoha.android

object GlobalVar {


    const val LANG_EN = "en"
    const val LANG_MR = "mr"
    const val LANG_HI = "hi"
    const val TYPE_WORD = "word"
    const val TYPE_GOOGLE_AD = "google-ad"
    const val CATEGORY_MOST_VIEWED = "most-viewed"
    const val CATEGORY_TOP_LIKED = "top-liked"
    val DEBUG_MODE = BuildConfig.DEBUG
    private val languages = arrayListOf<String>()

    fun loadLanguage(languages: List<String>) {
        this.languages.clear()
        this.languages.addAll(languages)
    }

    fun getLanguages() = languages
}
