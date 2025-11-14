package com.example.bisit.ui.login

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.bisit.R
import com.example.bisit.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_login) as NavHostFragment
        navController = navHostFragment.navController

        // 1. 툴바의 뒤로가기 버튼 클릭 리스너를 수동으로 설정
        binding.toolbar.setNavigationOnClickListener {
            // NavController에서 뒤로 갈 수 있으면 popBackStack, 아니면 Activity 종료
            if (!navController.popBackStack()) {
                finish()
            }
        }

        // 2. Fragment가 변경될 때마다 툴바를 수동으로 제어
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // 로그인 메인 화면
                R.id.loginCredentialsFragment -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbarTitle.text = "" // SignUp처럼 빈 제목
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
                // 아이디 찾기 화면
//                R.id.findIdFragment -> {
//                    binding.toolbar.visibility = View.VISIBLE
//                    binding.toolbarTitle.text = "아이디 찾기" // nav_graph_login의 label과 동일하게
//                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
//                }
//                // 비밀번호 찾기 화면
//                R.id.findPasswordFragment -> {
//                    binding.toolbar.visibility = View.VISIBLE
//                    binding.toolbarTitle.text = "비밀번호 찾기" // nav_graph_login의 label과 동일하게
//                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
//                }
                // 그 외 (혹시 모를)
                else -> {
                    binding.toolbar.visibility = View.VISIBLE
                    binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_back)
                }
            }
        }
    }
}