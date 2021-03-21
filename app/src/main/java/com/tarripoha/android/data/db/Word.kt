package com.tarripoha.android.data.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
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
@Entity(tableName = "word")
data class Word(
  @PrimaryKey
  val name: String,
  val meaning: String,
  var engMeaning: String? = null,
  var type: String? = null,
  var addedBy: String? = null,
  var popular: Double? = null,
  var dirty: Boolean? = null,
  var sentence: MutableList<Sentence>? = null
) : Parcelable {

  companion object {
    const val TYPE_NEW_WORD = "new_word"
  }

  constructor() : this("", "")
  constructor(
    name: String,
    type: String
  ) : this(name = name, type = type, meaning = "")
}