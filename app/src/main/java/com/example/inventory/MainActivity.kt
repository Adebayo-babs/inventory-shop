package com.example.inventory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Layout
import android.view.ContextMenu
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.inventory.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var navController : NavController
    private lateinit var binding: ActivityMainBinding

    fun setBottomNavigationVisibility(visibility: Int) {
        val bottomNavigationView = findViewById<LinearLayout>(R.id.catalog_dashboard_layout)
        bottomNavigationView.visibility = visibility
    }

    fun setBottomSellNavigationVisibility(visibility: Int) {
        val bottomNavigationView = findViewById<LinearLayout>(R.id.sell_bottom_nav_layout)
        bottomNavigationView.visibility = visibility
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment

        navController = navHostFragment.navController

//        val appBarConfiguration = AppBarConfiguration(setOf(
//            R.id.splashFragment, R.id.signInFragment, R.id.signUpFragment, R.id.welcomeFragment,
//            R.id.dashboardFragment, R.id.catalogFragment, R.id.sellFragment, R.id.analyticsFragment,
//            R.id.profileFragment
//        ))

        setupActionBarWithNavController(navController)
    }



    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

