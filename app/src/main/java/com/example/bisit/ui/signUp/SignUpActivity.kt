package com.example.bisit.ui.signUp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bisit.R
import com.example.bisit.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_sign_up) as NavHostFragment
        navController = navHostFragment.navController

        binding.toolbar.setNavigationOnClickListener {
            if (!navController.popBackStack()) {
                finish()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.termsDetailFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.text = arguments?.getString("termTitle") ?: "약관 상세"
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
                R.id.signUpInfoFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.text = ""
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
//                R.id.signUpCredentialsFragment -> {
//                    // 2단계: 툴바 보이기, "회원가입" 타이틀
//                    binding.toolbar.visibility = View.VISIBLE
//                    binding.toolbarTitle.text = "회원가입"
//                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
//                }
//                R.id.signUpCompleteFragment -> {
//                    // 3단계: 툴바 보이기, 타이틀 없고 뒤로가기 버튼도 없음
//                    binding.toolbar.visibility = View.VISIBLE
//                    binding.toolbarTitle.text = ""
//                    binding.toolbar.navigationIcon = null
//                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
            }
        }
    }
}