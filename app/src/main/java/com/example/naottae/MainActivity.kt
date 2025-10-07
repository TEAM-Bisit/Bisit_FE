package com.example.naottae

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.naottae.databinding.ActivityMainBinding

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

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        binding.bottomNavView.menu.clear()

        if (userType == "owner") {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu_owner)
            navGraph.setStartDestination(R.id.shopFragment)
        } else {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
            navGraph.setStartDestination(R.id.homeFragment)
        }

        navController.graph = navGraph

        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }
}