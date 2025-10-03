package com.example.naeottae

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.naeottae.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCustomer.setOnClickListener {
            startMainActivity("customer")
        }

        binding.btnOwner.setOnClickListener {
            startMainActivity("owner")
        }
    }

    private fun startMainActivity(userType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_TYPE", userType)
        startActivity(intent)
        finish()
    }
}