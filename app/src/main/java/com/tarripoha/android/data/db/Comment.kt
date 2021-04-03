package com.tarripoha.android.data.db

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

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
@Entity(tableName = "comment")
data class Comment(
  val id: String,
  val word: String,
  val comment: String,
  var userId: String? = null,
  var userName: String? = null,
  var popular: Double? = null,
  var dirty: Boolean? = null,
  var timestamp: Long? = null,
  var localStatus: Boolean? = null
) : Parcelable {
  constructor() : this(id = "", word = "", comment = "")
  constructor(
    id: String,
    word: String,
    comment: String,
    time: Long,
    userName: String
  ) : this(id = id, word = word, comment = comment, timestamp = time)
}