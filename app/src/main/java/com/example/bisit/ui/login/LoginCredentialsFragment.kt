package com.example.bisit.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher // TextWatcher 임포트
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentLoginCredentialsBinding

class LoginCredentialsFragment : Fragment() {

    private var _binding: FragmentLoginCredentialsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers() // 텍스트 변경 리스너 설정 추가
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            // TODO: 로그인 로직 구현 (ViewModel 호출)
        }

        binding.tvFindId.setOnClickListener {
            // 아이디 찾기 Fragment로 이동
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findIdFragment)
        }

        binding.tvFindPassword.setOnClickListener {
            // 비밀번호 찾기 Fragment로 이동
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findPasswordFragment)
        }
    }

    // 텍스트 변경 리스너 설정 메서드
    private fun setupTextWatchers() {
        // 아이디와 비밀번호 EditText에 동일한 TextWatcher를 적용
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때마다 버튼 활성화 상태를 업데이트
                updateLoginButtonState()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etId.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
    }

    // 로그인 버튼 활성화 상태 업데이트 메서드
    private fun updateLoginButtonState() {
        // 아이디와 비밀번호 필드가 모두 비어있지 않은지 확인
        val isIdValid = binding.etId.text.isNullOrBlank().not()
        val isPasswordValid = binding.etPassword.text.isNullOrBlank().not()

        // 두 조건이 모두 충족되면 버튼을 활성화
        binding.btnLogin.isEnabled = isIdValid && isPasswordValid
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}