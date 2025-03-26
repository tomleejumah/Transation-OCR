package com.example.translateocrapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ocr_results")
data class OcrResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imagePath: String?, // Store the image path, or use a ByteArray for storing the image itself
    val extractedText: String?,
    val detectedLanguage: String?,
    val translatedText: String?
)
