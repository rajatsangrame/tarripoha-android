package com.tarripoha.android.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 *
 *
 * Room model class to save comments on the Db
 *
 * Ref: https://developer.android.com/topic/libraries/architecture/room
 */

@Entity(tableName = "model")
class Model(
    @PrimaryKey(autoGenerate = true)
    @field:SerializedName("id")
    var id: Int = 0
)