package com.example.bisit.ui.signUp

import android.content.Intent // ✨ 1. Intent 임포트
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
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
            // 회원가입 프로세스 종료 후 로그인 화면(AuthActivity)으로 이동
            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}