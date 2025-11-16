package com.example.bisit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        // NavGraph 불러오기
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userType == "owner") {
            navGraph.setStartDestination(R.id.shopFragment)
        } else {
            navGraph.setStartDestination(R.id.customerCategoryFragment)
        }

        navController.graph = navGraph

        // BottomNavigation과 NavController 연결
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
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
