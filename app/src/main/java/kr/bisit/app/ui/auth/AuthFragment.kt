package kr.bisit.app.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.bisit.app.MainActivity
import kr.bisit.app.databinding.FragmentAuthBinding
import kr.bisit.app.ui.dialog.CommonInfoDialog
import kr.bisit.app.ui.login.LoginActivity
import kr.bisit.app.ui.login.LoginViewModel
import kr.bisit.app.ui.signUp.SignUpActivity

class AuthFragment : Fragment(), UserTypeDialog.UserTypeDialogListener {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val requiredTaps = 3
    private val tapTimeWindow: Long = 1000
    private val tapTimestamps = LongArray(requiredTaps)

    private val loginViewModel: LoginViewModel by viewModels()

    // 1️⃣ 소셜 로그인 결과 수신 런처
    private val socialLoginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val accessToken = data?.getStringExtra("ACCESS_TOKEN")
            val refreshToken = data?.getStringExtra("REFRESH_TOKEN")

            // socialLoginLauncher 내부 수정
            if (accessToken != null) {
                Log.d("LoginSuccess", "소셜 토큰 수신 완료 -> ViewModel로 전달")

                // [수정] requireContext()를 추가하고, refreshToken이 null일 경우 빈 문자열("") 등을 전달합니다.
                loginViewModel.handleSocialLoginSuccess(
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
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 로고 클릭 (디버그용)
        binding.logoImg.setOnClickListener { handleLogoTaps() }

        // 회원가입 버튼
        binding.signupText.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
                putExtra("START_DESTINATION", "INFO")
            }
            startActivity(intent)
        }

        // 일반 로그인 버튼
        binding.loginBtn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        // 네이버/카카오 로그인 버튼
        binding.naverBtn.setOnClickListener { launchSocialLogin("naver") }
        binding.kakaoBtn.setOnClickListener { launchSocialLogin("kakao") }

        // 2️⃣ 로그인 결과 관찰 (소셜 로그인과 일반 로그인 공통 사용)
        setupLoginObserver()
    }

    private fun launchSocialLogin(provider: String) {
        val intent = Intent(requireContext(), SocialLoginActivity::class.java).apply {
            putExtra("PROVIDER", provider)
        }
        socialLoginLauncher.launch(intent)
    }

    private fun setupLoginObserver() {
        loginViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // ViewModel이 처리를 완료하면 저장된 userType을 가져와 이동합니다.
                val userType = loginViewModel.userType.value
                handleUserTypeNavigation(userType)
            } else {
                val message = loginViewModel.errorMessage.value ?: "로그인에 실패했습니다."
                showErrorDialog(message)
            }
        }
    }

    private fun handleUserTypeNavigation(userType: String?) {
        val type = userType?.lowercase() ?: "none"
        Log.d("Navigation", "이동할 유저 타입: $type")

        if (type == "owner" || type == "customer") {
            // 이미 가입된 유저 -> 메인 화면으로 이동
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                putExtra("USER_TYPE", type)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        } else {
            // 신규 유저 ("none") -> 역할 선택(회원가입) 화면으로 이동
            val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    // --- 이하 로고 탭 및 다이얼로그 처리 로직 (기존과 동일) ---

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

    private fun showErrorDialog(msg: String) {
        CommonInfoDialog(message = msg, onConfirm = { }).show(parentFragmentManager, "ErrorDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}