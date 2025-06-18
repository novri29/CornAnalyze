package com.cornanalyze.cornanalyze

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.cornanalyze.cornanalyze.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setContentView(R.layout.activity_tutorial)
        enableEdgeToEdge()
        supportActionBar?.apply { // Mengatur action bar
            title = "Tutorial Penggunaan"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@TutorialActivity, R.color.transparent)))

            val textView = TextView(this@TutorialActivity).apply {
                text = getString(R.string.tutorial)
                setTextColor(Color.BLACK)
                textSize = 20f
                typeface = ResourcesCompat.getFont(this@TutorialActivity, R.font.poppins_medium)
            }
            setDisplayShowTitleEnabled(false)
            setCustomView(textView)
            setDisplayShowCustomEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> { // Mengatur tombol back ke home
                val intent = Intent(this, MainActivity::class.java).apply {
                    intent.putExtra("navigateTo", "HomeFragment")
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