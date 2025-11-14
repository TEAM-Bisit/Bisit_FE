package com.example.bisit.ui.signUp

import android.content.Intent // ✨ 1. Intent 임포트
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
// import androidx.navigation.NavOptions (삭제)
// import androidx.navigation.fragment.findNavController (삭제)
import com.example.bisit.databinding.FragmentSignUpCompleteBinding
import com.example.bisit.ui.auth.AuthActivity // ✨ 2. LoginActivity 경로 (추측)

class SignUpCompleteFragment : Fragment() {

    private var _binding: FragmentSignUpCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 👇 [수정됨] onViewCreated 로직 전체 변경
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnStart.setOnClickListener {
            // "시작하기" 버튼 클릭 시
            // LoginActivity로 이동하고, 현재 액티비티(회원가입) 스택을 종료합니다.

            // 1. LoginActivity로 가는 Intent 생성
            //    (LoginActivity::class.java를 사용하려면 import가 필요합니다)
            //    'LoginActivity'는 실제 로그인 액티비티 클래스 이름입니다.
            val intent = Intent(requireActivity(), AuthActivity::class.java)

            // 2. 새 태스크를 만들고, 기존 태스크(회원가입 화면들)를 모두 제거하는 플래그 설정
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // 3. LoginActivity 시작
            startActivity(intent)

            // 4. 현재 회원가입 액티비티 종료
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}