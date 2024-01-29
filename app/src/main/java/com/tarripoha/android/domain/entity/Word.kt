package com.tarripoha.android.domain.entity

import com.google.firebase.database.DatabaseException

data class Word(
    var id: String,
    val name: String,
    val meaning: String,
    var eng: String? = null,
    var otherDesc: String? = null,
    var lang: String? = null,
    var userId: String? = null,
    var createdAt: Long? = null,
    var updatedAt: Long? = null,
    var likes: Long? = null,
    var views: Long? = null,
    var comments: Long? = null,
    private var dirty: Boolean? = null,
    private var approved: Boolean? = null,
) {

    // Local use
    var isAddNewWord: Boolean = false

    /** Must define a no-argument constructor to avoid [DatabaseException] */
    constructor() : this(id = "", name = "", meaning = "")

    fun isDirty(): Boolean {
        val dirty = this.dirty
        return dirty != null && dirty
    }

    fun isApproved(): Boolean {
        val approved = this.approved
        return approved != null && approved
    }
}
