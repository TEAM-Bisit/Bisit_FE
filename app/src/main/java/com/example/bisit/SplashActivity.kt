package com.example.bisit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bisit.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(SPLASH_DELAY_MS)

            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isFirstRun = prefs.getBoolean("is_first_run", true)

            if (isFirstRun) {
                startActivity(Intent(this@SplashActivity, OnboardingActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }

            finish() // SplashActivity 종료
        }
    }

    companion object {
        private const val SPLASH_DELAY_MS = 3000L
    }
}