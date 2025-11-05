package com.example.bisit

import android.content.Intent // A/V: Intent 임포트 추가
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.bisit.databinding.ActivityLoginBinding
import com.example.bisit.ui.auth.AuthFragment

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AuthFragment.newInstance())
                .commitNow()
        }
    }

    fun navigateToMainActivity(userType: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("USER_TYPE", userType)
        startActivity(intent)
        finish()
    }
}