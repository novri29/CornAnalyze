package com.cornanalyze.cornanalyze

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.cornanalyze.cornanalyze.save.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val predictionId = intent.getLongExtra("ID_PREDICTION", -1)
        if (predictionId != -1L) {
            GlobalScope.launch(Dispatchers.Main) {
                val prediction = AppDatabase.getDatabase(applicationContext)
                    .predictionSaveDao().getPredictionById(predictionId)
                prediction?.let {
                    findViewById<TextView>(R.id.resultTextView).text = it.result
                    findViewById<TextView>(R.id.deskripsiTextView).text = it.description
                    findViewById<TextView>(R.id.saranTextView).text = it.advice
                    findViewById<TextView>(R.id.waktuPemindaianTextView).text = it.date

                    Glide.with(this@DetailActivity)
                        .load(it.imagePath)
                        .into(findViewById(R.id.iv_detail_image))
                }
            }
        } else {
            Toast.makeText(this, "Data tidak ditemukan!", Toast.LENGTH_SHORT).show()
        }
    }
}