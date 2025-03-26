package com.example.translateocrapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OcrResultAdapter(private var ocrResults: List<OcrResultEntity>) :
    RecyclerView.Adapter<OcrResultAdapter.OcrResultViewHolder>() {

    class OcrResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvImagePath: TextView = itemView.findViewById(R.id.tv_image_path)
        val tvExtractedText: TextView = itemView.findViewById(R.id.tv_extracted_text)
        val tvDetectedLanguage: TextView = itemView.findViewById(R.id.tv_detected_language)
        val tvTranslatedText: TextView = itemView.findViewById(R.id.tv_translated_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OcrResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ocr_result, parent, false)
        return OcrResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: OcrResultViewHolder, position: Int) {
        val ocrResult = ocrResults[position]
        holder.tvImagePath.text = "Image: ${ocrResult.imagePath}"
        holder.tvExtractedText.text = "Extracted: ${ocrResult.extractedText}"
        holder.tvDetectedLanguage.text = "Language: ${ocrResult.detectedLanguage}"
        holder.tvTranslatedText.text = "Translated: ${ocrResult.translatedText}"
    }

    override fun getItemCount(): Int = ocrResults.size

    // Method to update data
    fun updateData(newResults: List<OcrResultEntity>) {
        ocrResults = newResults
        notifyDataSetChanged()
    }
}