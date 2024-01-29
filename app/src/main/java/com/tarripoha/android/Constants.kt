package com.tarripoha.android

object Constants {


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