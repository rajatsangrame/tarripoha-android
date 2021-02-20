package com.tarripoha.android.data.db

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parceler
import kotlinx.android.parcel.Parcelize

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
class Word(
    @PrimaryKey
    var name: String,
    var meaning: String,
    var sentence: String
) : Parcelable