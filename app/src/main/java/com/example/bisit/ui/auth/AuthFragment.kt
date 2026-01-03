package com.example.bisit.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bisit.MainActivity
import com.example.bisit.databinding.FragmentAuthBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.login.LoginActivity
import com.example.bisit.ui.login.LoginViewModel
import com.example.bisit.ui.signUp.SignUpActivity

class AuthFragment : Fragment(), UserTypeDialog.UserTypeDialogListener {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val requiredTaps = 3
    private val tapTimeWindow: Long = 1000
    private val tapTimestamps = LongArray(requiredTaps)

    private val loginViewModel: LoginViewModel by viewModels()

    private val socialLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val accessToken = result.data?.getStringExtra("ACCESS_TOKEN")
            val refreshToken = result.data?.getStringExtra("REFRESH_TOKEN")

            if (accessToken != null && refreshToken != null) {
                // 추출한 토큰을 LoginViewModel을 통해 저장하고 후속 작업 진행
                loginViewModel.handleSocialLoginSuccess(requireContext(), accessToken, refreshToken)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoImg.setOnClickListener {
            handleLogoTaps()
        }

        binding.signupText.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
                // 유형 선택을 건너뛰고 정보 입력 화면으로 가라는 신호 전달
                putExtra("START_DESTINATION", "INFO")
            }
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.naverBtn.setOnClickListener {
            // TODO
        }

        binding.kakaoBtn.setOnClickListener {
            val intent = Intent(requireContext(), SocialLoginActivity::class.java)
            socialLoginLauncher.launch(intent)
        }

        loginViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                val userType = loginViewModel.userType.value
                when (userType) {
                    "owner", "customer" -> {
                        // 이미 가입된 유저라면 메인 화면으로 이동
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
                // 소셜 로그인 실패 시에도 상세 에러 코드에 따라 다이얼로그 처리 가능
                val code = loginViewModel.errorCode.value
                val message = loginViewModel.errorMessage.value ?: "소셜 로그인에 실패했습니다."

                // LoginCredentialsFragment에서 만든 showWrongPasswordDialog 등을 동일하게 호출
                showErrorDialog(message)
            }
        }
    }

    private fun handleLogoTaps() {
        System.arraycopy(tapTimestamps, 1, tapTimestamps, 0, tapTimestamps.size - 1)
        tapTimestamps[tapTimestamps.size - 1] = System.currentTimeMillis()

        if (tapTimestamps[0] >= (System.currentTimeMillis() - tapTimeWindow)) {
            tapTimestamps.fill(0)
            showUserTypeDialog()
        }
    }

    private fun showUserTypeDialog() {
        val dialog = UserTypeDialog.newInstance()
        dialog.setListener(this)
        dialog.show(parentFragmentManager, "UserTypeDialog")
    }

    override fun onUserTypeSelected(userType: String) {
        (activity as? AuthActivity)?.navigateToMainActivity(userType)
    }

    private fun launchSignUpActivityWithUserType() {
        val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
            putExtra("START_DESTINATION", "USER_TYPE")
        }
        startActivity(intent)
    }

    private fun showErrorDialog(msg: String) {
        val dialog = CommonInfoDialog(
            message = msg,
            onConfirm = { /* 확인 버튼 클릭 시 동작이 필요하면 여기에 작성 */ }
        )
        dialog.show(parentFragmentManager, "ErrorDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}
