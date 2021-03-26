package com.tarripoha.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type
import java.util.Date

class DbTypeConverter : Serializable {

  @TypeConverter fun toDate(dateLong: Long): Date {
    return Date(dateLong)
  }

  @TypeConverter fun fromDate(date: Date): Long {
    return date.time
  }

  @TypeConverter fun fromString(value: String): MutableList<Comment> {
    val listType: Type = object : TypeToken<MutableList<Comment>>() {}.type
    return Gson().fromJson(value, listType)
  }

  @TypeConverter fun fromList(list: MutableList<Comment>): String {
    val gson = Gson()
    return gson.toJson(list)
  }
}