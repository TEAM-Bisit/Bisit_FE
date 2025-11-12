package com.example.bisit.ui.signUp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
// import androidx.navigation.ui.AppBarConfiguration // 👈 이 줄 삭제
// import androidx.navigation.ui.setupWithNavController // 👈 이 줄 삭제
import com.example.bisit.R
import com.example.bisit.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavController 설정
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_sign_up) as NavHostFragment
        navController = navHostFragment.navController

        // ✨ 1. 툴바의 '뒤로가기' 버튼(navigationIcon)에 클릭 리스너를 직접 설정합니다.
        binding.toolbar.setNavigationOnClickListener {
            // 2. NavController가 스택에서 뒤로 갈 수 있으면 (예: 2단계 -> 1단계) 뒤로 가고,
            //    더 이상 뒤로 갈 수 없으면 (즉, 1단계 화면이면) Activity를 종료합니다.
            if (!navController.popBackStack()) {
                finish()
            }
        }

        // ✨ 3. 완료 화면에서는 뒤로가기 버튼 숨기기 (선택 사항)
        //    (3단계 완료 화면에서는 뒤로가기 버튼이 없는 것이 자연스럽습니다.)

    }
}