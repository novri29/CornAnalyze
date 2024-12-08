package com.cornanalyze.cornanalyze

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        try {
            // Ambil data dari Intent
            val hasilPrediksi = intent.getStringExtra("EXTRA_HASIL_PREDIKSI") ?: "Hasil tidak ditemukan"
            val saranPenanganan = intent.getStringExtra("EXTRA_SARAN") ?: "Saran tidak ditemukan"
            val waktuPemindaian = getCurrentTime()

            // Ambil URI gambar dari Intent
            val imageUriString = intent.getStringExtra("EXTRA_GAMBAR_URI")

            // Tambahkan log untuk debugging
            Log.d("ResultsActivity", "Image URI String: $imageUriString")

            val imageUri: Uri? = if (imageUriString != null) {
                try {
                    Uri.parse(imageUriString)
                } catch (e: Exception) {
                    Log.e("ResultsActivity", "Error parsing URI", e)
                    null
                }
            } else null

            // Temukan TextView dan ImageView di layout
            val hasilTextView: TextView = findViewById(R.id.hasilTextView)
            val saranTextView: TextView = findViewById(R.id.saranTextView)
            val waktuTextView: TextView = findViewById(R.id.waktuPemindaianTextView)
            val gambarImageView: ImageView = findViewById(R.id.gambarImageView)

            // Set teks ke TextView
            hasilTextView.text = hasilPrediksi
            saranTextView.text = saranPenanganan
            waktuTextView.text = waktuPemindaian

            // Menampilkan gambar jika URI ada
            imageUri?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.let { stream ->
                        val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                        gambarImageView.setImageBitmap(bitmap)
                        stream.close()
                    } ?: throw IllegalArgumentException("Input stream is null")
                } catch (e: Exception) {
                    Log.e("ResultsActivity", "Error loading image", e)
                    // Optionally, set a default/error image
                    gambarImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsActivity", "Unexpected error", e)
            // Handle any unexpected errors
            Toast.makeText(this, "Error loading results", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mendapatkan waktu saat ini
    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }
}