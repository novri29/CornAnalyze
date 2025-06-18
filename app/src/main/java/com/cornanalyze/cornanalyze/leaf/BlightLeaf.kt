package com.cornanalyze.cornanalyze.leaf

import android.content.Intent
import android.graphics.Color
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
import com.cornanalyze.cornanalyze.MainActivity
import com.cornanalyze.cornanalyze.R
import com.cornanalyze.cornanalyze.databinding.ActivityBlightLeafBinding

class BlightLeaf : AppCompatActivity() {
    private lateinit var binding: ActivityBlightLeafBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlightLeafBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        supportActionBar?.apply { // Mengatur action bar
            title = "Blight Leaf"
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_new_24)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@BlightLeaf, R.color.transparent)))

            val textView = TextView(this@BlightLeaf).apply {
                text = getString(R.string.blight_leaf)
                setTextColor(Color.BLACK)
                textSize = 20f
                typeface = ResourcesCompat.getFont(this@BlightLeaf, R.font.poppins_medium)

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
    // Navigasi kembali ke home
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