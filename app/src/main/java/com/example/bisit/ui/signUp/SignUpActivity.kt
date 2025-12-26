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

        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph_sign_up)
        val startDestination = intent.getStringExtra("START_DESTINATION")

        if (startDestination == "INFO") {
            // 회원가입 텍스트 클릭 시: 정보 입력 화면부터 시작
            navGraph.setStartDestination(R.id.signUpInfoFragment)
        } else {
            // 기본값 또는 USER_TYPE 신호 시: 유형 선택 화면부터 시작
            navGraph.setStartDestination(R.id.userTypeFragment)
        }

        navController.graph = navGraph

        binding.toolbar.setNavigationOnClickListener {
            if (!navController.popBackStack()) {
                finish()
            }
        }

        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.userTypeFragment,
                R.id.signUpCompleteFragment -> {
                    binding.toolbar.visibility = View.GONE
                }

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
                R.id.signUpCredentialsFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.text = ""
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
            }
        }
    }

    fun setToolbarTitle(title: String) {
        binding.toolbarTitle.text = title
    }
}