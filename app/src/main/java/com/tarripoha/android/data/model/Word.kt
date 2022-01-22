package com.tarripoha.android.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.DatabaseException

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 *
 *
 * Room model class to save comments on the Db
 *
 * Ref: https://developer.android.com/topic/libraries/architecture/room
 */

@Parcelize
@Entity(tableName = "word")
data class Word(
    @PrimaryKey
    val name: String,
    val meaning: String,
    var eng: String? = null,
    var otherDesc: String? = null,
    var type: String? = null, // Local
    var lang: String? = null,
    var addedByUserId: String? = null,
    var addedByUserName: String? = null,
    var popular: Double? = null,
    private var dirty: Boolean? = null,
    private var approved: Boolean? = false,
    var comments: List<Comment>? = null,
    var timestamp: Long? = null,
    var updated: Long? = null,
    var saved: Boolean? = false,
    var likes: MutableMap<String, Boolean>? = null,
    var views: MutableMap<String, MutableList<Long?>>? = null
    // Make sure to update edit method when ever extra parameter is added
) : Parcelable {

    companion object {
        const val TYPE_NEW_WORD = "new_word"
        fun getNewWord(name: String) = Word(name = name, type = TYPE_NEW_WORD)
    }

    /** Must define a no-argument constructor to avoid [DatabaseException] */
    constructor() : this(name = "", meaning = "")
    constructor(
        name: String,
        type: String
    ) : this(name = name, meaning = "", type = type)

    fun updateUserRelatedData(user: User) {
        this.addedByUserId = user.id
        this.addedByUserName = user.name
        this.approved = user.admin
    }

    fun isDirty(): Boolean {
        val dirty = this.dirty
        return dirty != null && dirty
    }

    fun isApproved(): Boolean {
        val approved = this.approved
        return approved != null && approved
    }

    fun edit(meaning: String, engMeaning: String, otherDesc: String?, lang: String?): Word {
        return Word(
            name = this.name,
            meaning = meaning,
            eng = engMeaning,
            otherDesc = otherDesc,
            lang = lang ?: this.lang,
            addedByUserId = this.addedByUserId,
            addedByUserName = this.addedByUserName,
            popular = this.popular,
            dirty = this.dirty,
            approved = this.approved,
            comments = this.comments,
            timestamp = this.timestamp,
            saved = this.saved,
            likes = this.likes,
            views = this.views
        )
    }
}
