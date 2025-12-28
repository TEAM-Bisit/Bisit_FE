package com.example.bisit

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.bisit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 상태바 흰색 배경, 아이콘 어둡게 설정
        setupStatusBar()

        val userType = intent.getStringExtra("USER_TYPE")

        binding.bottomNavView.menu.clear()

        if (userType == "owner") {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu_owner)
        } else {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userType == "owner") {
            navGraph.setStartDestination(R.id.shopFragment)
        } else {
            navGraph.setStartDestination(R.id.customerCategoryFragment)
        }

        navController.graph = navGraph
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }

    private fun setupStatusBar() {
        // 상태바 배경 흰색
        window.statusBarColor = android.graphics.Color.WHITE
        
        // 상태바 아이콘 어둡게 (어두운 아이콘 = light status bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (API 30) 이상
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            // Android 6.0 (API 23) ~ Android 10 (API 29)
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = 
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    fun logout() {
        binding.bottomNavView.visibility = View.GONE

        val navController = findNavController(R.id.nav_host_fragment)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        navGraph.setStartDestination(R.id.authFragment)

        navController.setGraph(navGraph, null)
    }

    fun moveToHomeAfterLogin(userType: String) {
        binding.bottomNavView.visibility = View.VISIBLE

        val navController = findNavController(R.id.nav_host_fragment)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userType == "owner") {
            navGraph.setStartDestination(R.id.shopFragment)
        } else {
            navGraph.setStartDestination(R.id.customerCategoryFragment)
        }

        navController.setGraph(navGraph, null)
    }
}
