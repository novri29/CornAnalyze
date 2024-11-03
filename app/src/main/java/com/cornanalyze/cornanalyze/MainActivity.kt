package com.cornanalyze.cornanalyze

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.cornanalyze.cornanalyze.fragments.HistoryFragment
import com.cornanalyze.cornanalyze.fragments.HomeFragment
import com.cornanalyze.cornanalyze.fragments.ScanFragment
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<CurvedBottomNavigation>(R.id.bottomNavigation)
        bottomNavigation.add(
            CurvedBottomNavigation.Model(1,"Home", R.drawable.baseline_home_24)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(2,"Scan", R.drawable.baseline_home_24)
        )
        bottomNavigation.add(
            CurvedBottomNavigation.Model(3,"History", R.drawable.baseline_home_24)
        )

        bottomNavigation.setOnClickMenuListener {
            when(it.id) {
                1 -> {
                    replaceFragment(HomeFragment())
                }
                2 -> {
                    replaceFragment(ScanFragment())
                }
                3 -> {
                    replaceFragment(HistoryFragment())
                }
            }
        }

        //default bottom navigation selected
        replaceFragment(HomeFragment())
        bottomNavigation.show(1)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}