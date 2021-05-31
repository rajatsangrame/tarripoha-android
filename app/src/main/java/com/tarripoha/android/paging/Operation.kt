package com.tarripoha.android.paging

import com.tarripoha.android.data.db.Comment

data class Operation(
    val comment: Comment,
    val type: Int
)