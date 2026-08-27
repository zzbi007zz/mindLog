package com.example.diary.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [EntryEntity::class, EntryImageEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class DiaryDatabase : RoomDatabase() {

    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun get(context: Context): DiaryDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary.db",
                ).build().also { INSTANCE = it }
            }
    }
}