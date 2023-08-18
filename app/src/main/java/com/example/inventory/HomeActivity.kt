package com.example.inventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.example.inventory.appFragments.AnalyticsFragment
import com.example.inventory.appFragments.CatalogFragment
import com.example.inventory.appFragments.DashboardFragment
import com.example.inventory.appFragments.ProfileFragment
import com.example.inventory.appFragments.SellFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    val dashboardFragment = DashboardFragment()
    val sellFragment = SellFragment()
    val catalogFragment = CatalogFragment()
    val analyticsFragment = AnalyticsFragment()
    val profileFragment = ProfileFragment()

    fun setBottomNavigationVisibility(visibility: Int) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility = visibility
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        replaceFragment(dashboardFragment)

        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.dashboardFragment -> replaceFragment(dashboardFragment)
                R.id.sellFragment -> replaceFragment(sellFragment)
                R.id.catalogFragment -> replaceFragment(catalogFragment)
                R.id.analyticsFragment -> replaceFragment(analyticsFragment)
                R.id.profileFragment -> replaceFragment(profileFragment)
            }
            true
        }


    }



    private fun replaceFragment(fragment: Fragment) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.commit()
        }
    }
}