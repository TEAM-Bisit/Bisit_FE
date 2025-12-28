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
            // ✨ Intent 대신 Navigation Action을 사용하여 유형 선택(UserType)으로 이동
            findNavController().navigate(R.id.action_signUpCompleteFragment_to_userTypeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}