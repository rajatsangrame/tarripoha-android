package com.tarripoha.android.data.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import com.google.firebase.database.DatabaseException
import com.tarripoha.android.data.model.User

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
    var type: String? = null,
    var lang: String? = null,
    var addedByUserId: String? = null,
    var addedByUserName: String? = null,
    var popular: Double? = null,
    var dirty: Boolean? = null,
    var approved: Boolean? = null,
    var comments: List<Comment>? = null,
    var timestamp: Long? = null,
    var updated: Long? = null
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
    }

    fun edit(meaning: String, engMeaning: String, otherDesc: String?, lang: String?): Word {
        return Word(
            name = this.name,
            meaning = meaning,
            eng = engMeaning,
            otherDesc = otherDesc,
            lang = lang ?: this.lang,
            timestamp = this.timestamp,
            addedByUserId = this.addedByUserId,
            addedByUserName = this.addedByUserName,
            popular = this.popular,
            dirty = this.dirty,
            approved = this.approved,
            comments = this.comments
        )
    }
}
