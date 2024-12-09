package com.cornanalyze.cornanalyze

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cornanalyze.cornanalyze.databinding.ActivityResultsBinding
import com.cornanalyze.cornanalyze.save.AppDatabase
import com.cornanalyze.cornanalyze.save.PredictionSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        try {
            // Ambil data dari Intent
            val hasilPrediksi = intent.getStringExtra("EXTRA_HASIL_PREDIKSI") ?: "Hasil tidak ditemukan"
            val saranPenanganan = intent.getStringExtra("EXTRA_SARAN") ?: "Saran tidak ditemukan"
            val deskripsiPenyakit = intent.getStringExtra("EXTRA_DESKRIPSI") ?: "Deskripsi tidak tersedia"
            val waktuPemindaian = getCurrentTime()

            // URI gambar
            val imageUriString = intent.getStringExtra("EXTRA_GAMBAR_URI")
            val imageUri = imageUriString?.let { Uri.parse(it) }

            // Menampilkan hasil di UI
            binding.hasilTextView.text = hasilPrediksi
            binding.deskripsiTextView.text = deskripsiPenyakit
            binding.saranTextView.text = saranPenanganan
            binding.waktuPemindaianTextView.text = waktuPemindaian

            // Load gambar yang sudah dipotong
            imageUri?.let {
                try {
                    val inputStream = contentResolver.openInputStream(it)
                    inputStream?.use { stream ->
                        val bitmap: Bitmap = BitmapFactory.decodeStream(stream)
                        binding.gambarImageView.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    Log.e("ResultsActivity", "Error loading image", e)
                    binding.gambarImageView.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }

            // Tombol simpan
            binding.save.setOnClickListener {
                if (imageUri != null && hasilPrediksi.isNotEmpty()) {
                    // Simpan gambar yang sudah dipotong
                    val croppedImageUri = saveBitmapToInternalStorage(BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri)))
                    savePredictionToDatabase(croppedImageUri, hasilPrediksi)
                } else {
                    showToast("Gambar atau hasil prediksi tidak valid!")
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsActivity", "Error in onCreate", e)
            Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCurrentTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun savePredictionToDatabase(imageUri: Uri, result: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Simpan URI gambar yang sudah dipotong
                val prediction = PredictionSave(imagePath = imageUri.toString(), result = result)
                val database = AppDatabase.getDatabase(applicationContext)
                database.predictionSaveDao().insertPrediction(prediction)
                Log.d("ResultsActivity", "Prediction saved: $prediction")

                val allPredictions = database.predictionSaveDao().getALLPrediction()
                Log.d("ResultsActivity", "All predictions: $allPredictions")

                showToast("Data berhasil disimpan!")
            } catch (e: Exception) {
                Log.e("ResultsActivity", "Failed to save prediction", e)
                showToast("Gagal menyimpan data.")
            }
        }
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri {
        val fileName = "cropped_image_${System.currentTimeMillis()}.jpg"
        val file = File(filesDir, fileName)
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            return Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            return Uri.EMPTY
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
