package com.example.translateocrapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OcrResultEntity::class], version = 1, exportSchema = false)
abstract class OcrDatabase : RoomDatabase() {
    abstract fun ocrResultDao(): OcrResultDao

    companion object {
        @Volatile
        private var INSTANCE: OcrDatabase? = null

        fun getDatabase(context: Context): OcrDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OcrDatabase::class.java,
                    "ocr_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
