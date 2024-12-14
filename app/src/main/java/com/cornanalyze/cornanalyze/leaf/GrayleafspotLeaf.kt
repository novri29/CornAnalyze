package com.cornanalyze.cornanalyze.leaf

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.cornanalyze.cornanalyze.MainActivity
import com.cornanalyze.cornanalyze.R
import com.cornanalyze.cornanalyze.databinding.ActivityBlightLeafBinding
import com.cornanalyze.cornanalyze.databinding.ActivityGrayleafspotLeafBinding

class GrayleafspotLeaf : AppCompatActivity() {
    private lateinit var binding: ActivityGrayleafspotLeafBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrayleafspotLeafBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        supportActionBar?.apply {
            title = "Gray Leaf Spot"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@GrayleafspotLeaf, R.color.transparent)))

            val textView = TextView(this@GrayleafspotLeaf).apply {
                text = getString(R.string.GrayLeafSpot_Leaf)
                setTextColor(Color.BLACK)
                textSize = 20f
                typeface = ResourcesCompat.getFont(this@GrayleafspotLeaf, R.font.poppins_medium)

                // Tambahkan pengaturan untuk membuat teks di tengah
                layoutParams = ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER
                )
            }
            setDisplayShowTitleEnabled(false)
            setCustomView(textView)
            setDisplayShowCustomEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
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