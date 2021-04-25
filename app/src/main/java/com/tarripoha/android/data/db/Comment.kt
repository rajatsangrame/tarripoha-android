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
  var userId: String,
  var timestamp: Double = 0.0,
  var popular: Double = 0.0,
  var userName: String? = null,
  var dirty: Boolean = false,
  var localStatus: Boolean? = null
) : Parcelable {
  constructor() : this(id = "", word = "", comment = "", timestamp = 0.0, userId = "")
  constructor(
    id: String,
    word: String,
    comment: String,
    time: Double,
    userName: String
  ) : this(id = id, word = word, comment = comment, timestamp = time, userId = "")
}
