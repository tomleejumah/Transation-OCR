package com.example.translateocrapp

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog

import com.google.mlkit.vision.text.Text

// Import coroutines dependencies
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import com.example.translateocrapp.OcrHelper
import com.example.translateocrapp.LanguageRecognizer
import com.example.translateocrapp.TextTranslator
import com.example.translateocrapp.BitmapAnnotator

class PreviewActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }


    private lateinit var previewImageView: ImageView
    private lateinit var backButton: Button

    private lateinit var bitmap: Bitmap

    // Create an instance of the OcrHelper class
    private val ocrHelper = OcrHelper()

    // Create an instance of the LanguageRecognizer class
    private val languageRecognizer = LanguageRecognizer()

    // Create an instance of the TextTranslator class
    private val textTranslator = TextTranslator(this)

    // Create a variable to store the OCR result
    private lateinit var ocrResultMap: Map<Rect, Text.TextBlock>

    // Create a variable to store the language detected
    private lateinit var languageCode: String

    // Create a variable to store the translated ocr result
    private lateinit var translatedOcrResultMap: Map<Rect, String>

    // Job variable to keep track of the OCR job
    private lateinit var ocrJob: Job

    // Job variable to keep track of the language identification job
    private lateinit var languageJob: Job

    // Create a progress dialog
    private lateinit var progressBar: ProgressBar

    // AlertDialog to show download progress
    private var progressDialog: AlertDialog? = null
    private lateinit var database: OcrDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        database = OcrDatabase.getDatabase(this)

        previewImageView = findViewById(R.id.previewImageView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)

        // Initialize the progress bar
        progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        // Show the progress dialog
        Handler(Looper.getMainLooper()).post {
            progressDialog = AlertDialog.Builder(this)
                .setTitle("Translating ...")
                .setCancelable(false)
                .setView(progressBar)
                .show()
        }

        if (imagePath != null) {
            // Get the bitmap from the image file
            bitmap = readImageFile(imagePath)

            // Display the bitmap
            displayBitmap(bitmap)

            // Perform all operations in a single coroutine chain
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Step 1: Perform OCR in background
                    val ocrResult = withContext(Dispatchers.Default) {
                        ocrHelper.performOcr(bitmap)
                    }
                    ocrResultMap = ocrResult // Update the class-level variable
                    processOcrResult(ocrResult)

                    // Step 2: Recognize language in background
                    val detectedLanguage = withContext(Dispatchers.Default) {
                        languageRecognizer.recognizeLanguage(ocrResult)
                    }
                    languageCode = detectedLanguage // Update the class-level variable
                    processLanguageResult(detectedLanguage)

                    // Step 3: Translate text in background
                    val translatedResult = withContext(Dispatchers.Default) {
                        textTranslator.translateOcrResult(ocrResult, detectedLanguage)
                    }
                    translatedOcrResultMap = translatedResult // Update the class-level variable
                    processTranslationResult(translatedResult)

                    // Step 4: Save to database in IO thread
                    withContext(Dispatchers.IO) {
                        // Extract text from ocrResultMap (Map<Rect, Text.TextBlock>)
                        val extractedText = ocrResultMap.values.joinToString(" ") { it.text }

                        // Extract text from translatedOcrResultMap (Map<Rect, String>)
                        val translatedText = translatedOcrResultMap.values.joinToString(" ")


                            val ocrResultEntity = OcrResultEntity(
                                imagePath = imagePath,
                                extractedText = extractedText,
                                detectedLanguage = detectedLanguage,
                                translatedText = translatedText
                            )
                          if (!ocrResultEntity.detectedLanguage.equals("und")) {
                              database.ocrResultDao().insertOcrResult(ocrResultEntity)
                          } else {
                              Log.d("TAG", "onCreate: ")
                          }


                    }

                    // Dismiss progress dialog
                    progressDialog?.dismiss()

                } catch (e: Exception) {
                    Log.e("PreviewActivity", "Error processing OCR: $e")
                    progressDialog?.dismiss()
//                    Toast.makeText(this@PreviewActivity, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
    // Create a function to process the OCR result
    private fun processOcrResult(ocrResultMap: Map<Rect, Text.TextBlock>) {
        // Log the OCR result with Rect and text
        for ((rect, textBlock) in ocrResultMap) {
            Log.d("OCR", "Found text ${textBlock.text} at $rect")

        }

    }

    // Create a function to process the language identification result
    private fun processLanguageResult(languageResult: String) {
        // Handle the language identification result
        Log.d("Language", "Language detected is $languageCode")

    }

    // Create a function to process the translation result
    private fun processTranslationResult(translatedText: Map<Rect, String>) {
        // Handle the translation result
        for ((rect, text) in translatedText) {
//            Log.d("Translation", "Translated text $text at $rect")
        }
        Log.d("Translation", "Language detected is $languageCode")
        Log.d("Translation", "Translated text is $translatedText")

        // Get annotated bitmap
        bitmap = BitmapAnnotator.annotateBitmap(bitmap, ocrResultMap, translatedText)

        // Display the annotated bitmap
        displayBitmap(bitmap)

        // Dismiss the progress dialog
        Handler(Looper.getMainLooper()).post {
            progressDialog?.dismiss()
            progressDialog = null
        }

    }

    // Create a function to read image file and return bitmap
    private fun readImageFile(imagePath: String): Bitmap {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        val bitmap = BitmapFactory.decodeFile(imagePath, options)
        // Rotate the bitmap if required and return it
        return rotateBitmap(imagePath, bitmap)
    }

    // Create a function to display the bitmap
    private fun displayBitmap(bitmap: Bitmap) {
        previewImageView.setImageBitmap(bitmap)
    }

    private fun rotateBitmap(imagePath: String, bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        return if (rotationDegrees != 0) {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(rotationDegrees.toFloat())
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

}