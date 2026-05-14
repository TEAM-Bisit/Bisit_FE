package kr.bisit.app.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
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