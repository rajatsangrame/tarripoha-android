package com.tarripoha.android.data.model

data class FAQResponse(

    val faq: List<FAQ>
)

data class FAQ(
    val lang: String,
    val title: String,
    val desc: String,
    var localExpandedState: Boolean = false
)
