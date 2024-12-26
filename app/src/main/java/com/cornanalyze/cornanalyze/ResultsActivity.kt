package com.cornanalyze.cornanalyze

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
        enableEdgeToEdge()
        supportActionBar?.apply {
            title = "CornAnalyze"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@ResultsActivity, R.color.transparent)))

            val textView = TextView(this@ResultsActivity).apply {
                text = getString(R.string.result)
                setTextColor(Color.BLACK)
                textSize = 20f
                typeface = ResourcesCompat.getFont(this@ResultsActivity, R.font.poppins_medium)
            }
            setDisplayShowTitleEnabled(false)
            setCustomView(textView)
            setDisplayShowCustomEnabled(true)
        }

        try {
            // Ambil data dari Intent
            val hasilPrediksi = intent.getStringExtra("EXTRA_HASIL_PREDIKSI") ?: "Hasil tidak ditemukan"
            val deskripsiPenyakit = intent.getStringExtra("EXTRA_DESKRIPSI") ?: "Tidak ada informasi tersedia."
            val deskripsiPenyebab = intent.getStringExtra("EXTRA_PENYEBAB") ?: "Tidak ada penyebab ditemukan"
            val saranPenanganan = intent.getStringExtra("EXTRA_SARAN") ?: "Tidak ada langkah penanganan."
            val sumber = intent.getStringExtra("EXTRA_SUMBER") ?: "Tidak ada sumber informasi."
            val waktuPemindaian = getCurrentTime()

            // URI gambar
            val imageUriString = intent.getStringExtra("EXTRA_GAMBAR_URI")
            val imageUri = imageUriString?.let { Uri.parse(it) }

            // Menampilkan hasil di UI
            binding.hasilTextView.text = hasilPrediksi
            binding.deskripsiTextView.text = deskripsiPenyakit
            binding.penyebabTextView.text = deskripsiPenyebab
            binding.saranTextView.text = saranPenanganan
            binding.sumberTextView.text = sumber
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
                    val croppedImageUri = saveBitmapToInternalStorage(BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri)))
                    savePredictionToDatabase(
                        croppedImageUri,
                        hasilPrediksi,
                        deskripsiPenyakit,
                        deskripsiPenyebab,
                        saranPenanganan,
                        sumber,
                        waktuPemindaian
                    )
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

    private fun savePredictionToDatabase(imageUri: Uri, result: String, description: String, cause : String, advice: String, source: String, date: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Membuat entitas PredictionSave
                val prediction = PredictionSave(
                    imagePath = imageUri.toString(),
                    result = result,
                    description = description,
                    cause = cause,
                    advice = advice,
                    source = source,
                    date = date
                )
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    intent.putExtra("navigateTo", "HistoryFragment")
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
