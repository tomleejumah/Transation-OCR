package com.example.translateocrapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OcrResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOcrResult(ocrResult: OcrResultEntity)

    @Query("SELECT * FROM ocr_results ORDER BY id DESC")
    suspend fun getAllOcrResults(): List<OcrResultEntity>
}
