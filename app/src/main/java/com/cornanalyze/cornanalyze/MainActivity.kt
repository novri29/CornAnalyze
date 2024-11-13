package com.cornanalyze.cornanalyze

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.cornanalyze.cornanalyze.databinding.ActivityMainBinding
import com.cornanalyze.cornanalyze.fragments.HistoryFragment
import com.cornanalyze.cornanalyze.fragments.HomeFragment
import com.cornanalyze.cornanalyze.fragments.ScanFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.background = null

        //ke menu home
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), true)
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.menu_home -> replaceFragment(HomeFragment(), true)
                R.id.menu_scan -> replaceFragment(ScanFragment(), true)
                R.id.menu_history -> replaceFragment(HistoryFragment(), true)
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, showBottomNav: Boolean, addToBackStack: Boolean = false): Boolean {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()

        if (showBottomNav) {
            binding.bottomNavigationView.visibility = android.view.View.VISIBLE
        } else {
            binding.bottomNavigationView.visibility = android.view.View.GONE
        }
        return true
    }
}