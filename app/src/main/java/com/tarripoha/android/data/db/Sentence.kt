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
@Entity(tableName = "sentence")
data class Sentence(
  val word: String,
  val sentence: String,
  var addedBy: String? = null,
  var popular: Double? = null,
  var dirty: Boolean = false
) : Parcelable {
  constructor() : this("", "")
}