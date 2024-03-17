package com.tarripoha.android

object Constants {


    const val TYPE_WORD = "word"
    const val TYPE_GOOGLE_AD = "google-ad"
    const val CATEGORY_MOST_VIEWED = "most-viewed"
    const val CATEGORY_TOP_LIKED = "top-liked"
    const val CATEGORY_USER_LIKED = "user-liked"
    const val CATEGORY_USER_REQUESTED = "user-requested"
    const val CATEGORY_PENDING_APPROVALS = "pending-approvals"
    const val CATEGORY_SAVED = "saved"
    const val LANG_EN = "en"
    const val LANG_MAR = "mar"
    const val LANG_HI = "hi"
    const val LANG_ANY = "any"
    val DEBUG_MODE = BuildConfig.DEBUG

    fun getLanguageName(langCode: String): String? {
        val map = mutableMapOf<String, String>()
        map["en"] = "English"
        map["mr"] = "Marathi"
        map["hn"] = "Hindi"
        return map[langCode]
    }

    fun getLanguageCode(lang: String): String? {
        val map = mutableMapOf<String, String>()
        map["English"] = "en"
        map["Marathi"] = "mr"
        map["Hindi"] = "hn"
        return map[lang]
    }

    enum class DashboardViewCategory(val value: String) {
        MOST_VIEWED("most_viewed"),
        MOST_LIKED("top_liked"),
        USER_LIKED("user_liked"),
        USER_REQUESTED("user_requested"),
        PENDING_APPROVALS("pending_approvals"),
        USER_SAVED("user_saved")
    }

    enum class DashboardViewType(val value: String) {
        TYPE_WORD("word"),
        TYPE_GOOGLE_AD("google-ad")
    }
}