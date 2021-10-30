package com.tarripoha.android.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type
import java.util.Date

class DbTypeConverter : Serializable {

    @TypeConverter
    fun toDate(dateLong: Long): Date {
        return Date(dateLong)
    }

    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun fromString(value: String): MutableList<Comment> {
        val listType: Type = object : TypeToken<MutableList<Comment>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: MutableList<Comment>): String {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun stringToLikeMap(value: String): Map<String, Boolean> {
        return Gson().fromJson(value, object : TypeToken<Map<String, Boolean>>() {}.type)
    }

    @TypeConverter
    fun likeMapToString(value: Map<String, Boolean>?): String {
        return if (value == null) "" else Gson().toJson(value)
    }

    @TypeConverter
    fun stringToViewsMap(value: String): MutableMap<String, List<Long>> {
        return Gson().fromJson(value, object : TypeToken<Map<String, Boolean>>() {}.type)
    }

    @TypeConverter
    fun viewsMapToString(value: MutableMap<String, List<Long>>?): String {
        return if (value == null) "" else Gson().toJson(value)
    }
}
