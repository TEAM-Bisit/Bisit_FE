package com.example.bisit.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity // 메인 액티비티 임포트
import com.example.bisit.R
import com.example.bisit.databinding.FragmentLoginCredentialsBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.dialog.CustomDialog
import com.example.bisit.ui.dialog.CustomTwoButtonDialog
import com.example.bisit.ui.signUp.SignUpActivity
import com.example.bisit.ui.auth.SocialLoginActivity

class LoginCredentialsFragment : Fragment() {

    private var _binding: FragmentLoginCredentialsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    private val socialLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val accessToken = data?.getStringExtra("ACCESS_TOKEN")
            val refreshToken = data?.getStringExtra("REFRESH_TOKEN")

            if (accessToken != null) {
                Log.d("LoginSuccess", "소셜 토큰 수신 완료 -> ViewModel로 전달")
                viewModel.handleSocialLoginSuccess(
                    requireContext(),
                    accessToken,
                    refreshToken ?: ""
                )
            }
        }
    }

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
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPassword.text.toString()

            // ViewModel에 로그인 요청
            viewModel.login(requireContext(), id, pw)
        }

        binding.tvFindId.setOnClickListener {
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findIdFragment)
        }

        binding.tvFindPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findPasswordFragment)
        }

        binding.naverBtn.setOnClickListener { launchSocialLogin("naver") }
        binding.kakaoBtn.setOnClickListener { launchSocialLogin("kakao") }
    }

    private fun launchSocialLogin(provider: String) {
        val intent = Intent(requireContext(), SocialLoginActivity::class.java).apply {
            putExtra("PROVIDER", provider)
        }
        socialLoginLauncher.launch(intent)
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                val userType = viewModel.userType.value
                when (userType) {
                    "owner", "customer" -> {
                        val intent = Intent(requireContext(), MainActivity::class.java).apply {
                            putExtra("USER_TYPE", userType)
                        }
                        startActivity(intent)
                        activity?.finish()
                    }
                    "none" -> {
                        val intent = Intent(requireContext(), SignUpActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    }
                }
            } else {
                val code = viewModel.errorCode.value
                val message = viewModel.errorMessage.value ?: "오류가 발생했습니다."

                when (code) {
                    "AUTH400" -> {
                        // 1. 비밀번호 틀림 -> 제공해주신 dialog_common_info 사용
                        showWrongPasswordDialog("비밀번호를 확인해주세요")
                    }
                    "COMMON404" -> {
                        // 2. 계정 없음 -> 2버튼 다이얼로그 (생성하기 포함)
                        showNoAccountDialog()
                    }
                    else -> {
                        // 기타 에러
                        showWrongPasswordDialog(message)
                    }
                }
            }
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etId.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
    }

    private fun updateLoginButtonState() {
        val isIdValid = binding.etId.text.isNullOrBlank().not()
        val isPasswordValid = binding.etPassword.text.isNullOrBlank().not()
        binding.btnLogin.isEnabled = isIdValid && isPasswordValid
    }

    private fun showWrongPasswordDialog(msg: String) {
        val dialog = CommonInfoDialog(
            message = msg,
            onConfirm = { /* 닫기 클릭 시 추가 동작 필요하면 작성 */ }
        )
        dialog.show(parentFragmentManager, "WrongPasswordDialog")
    }

    private fun showNoAccountDialog() {
        val dialog = CustomTwoButtonDialog(
            title = "계정 정보가 없어요",
            subtitle = "새로운 계정을 생성하시겠어요?",
            positiveButtonText = "생성하기",
            negativeButtonText = "닫기",
            onPositiveClick = {
                val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
                    putExtra("START_DESTINATION", "INFO")
                }
                startActivity(intent)
            }
        )
        dialog.show(parentFragmentManager, "NoAccountDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}