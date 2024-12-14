package com.cornanalyze.cornanalyze

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.cornanalyze.cornanalyze.databinding.ActivityDetailBinding
import com.cornanalyze.cornanalyze.save.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_detail)
        enableEdgeToEdge()
        supportActionBar?.apply {
            title = "CornAnalyze"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@DetailActivity, R.color.transparent)))

            val textView = TextView(this@DetailActivity).apply {
                text = getString(R.string.result)
                setTextColor(Color.BLACK)
                textSize = 20f
                typeface = ResourcesCompat.getFont(this@DetailActivity, R.font.poppins_medium)
            }
            setDisplayShowTitleEnabled(false)
            setCustomView(textView)
            setDisplayShowCustomEnabled(true)
        }

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