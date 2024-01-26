package com.tarripoha.android.domain.entities

data class Word(
    var title: String,
    var content: String,
    var creationTime: Long,
    var updatedTime: Long,
    var id: Long
)
