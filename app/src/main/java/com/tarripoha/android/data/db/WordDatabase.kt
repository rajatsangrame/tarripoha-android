package com.tarripoha.android.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.tarripoha.android.data.model.Word

/**
 * Created by Rajat Sangrame
 * http://github.com/rajatsangrame
 *
 * Database class for the application. Mange the database in here.
 *
 * Ref: https://developer.android.com/topic/libraries/architecture/room
 * Ref: https://developer.android.com/training/data-storage/room/migrating-db-versions
 */
@Database(entities = [Word::class], version = 1)
@TypeConverters(DbTypeConverter::class)
abstract class WordDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    companion object {
        @Volatile
        private var INSTANCE: WordDatabase? = null

        fun getDataBase(context: Context): WordDatabase? {
            if (INSTANCE == null) {
                synchronized(WordDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        WordDatabase::class.java, "database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}