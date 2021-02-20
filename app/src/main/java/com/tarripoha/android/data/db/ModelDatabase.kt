package com.tarripoha.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 *
 * Database class for the application. Mange the database in here.
 *
 * Ref: https://developer.android.com/topic/libraries/architecture/room
 */
@Database(entities = [Model::class], version = 1)
@TypeConverters(DbTypeConverter::class)
abstract class ModelDatabase : RoomDatabase() {

    abstract fun commentDao(): ModelDao

    companion object {
        @Volatile
        private var INSTANCE: ModelDatabase? = null

        fun getDataBase(context: Context): ModelDatabase? {
            if (INSTANCE == null) {
                synchronized(Database::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ModelDatabase::class.java, "database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}