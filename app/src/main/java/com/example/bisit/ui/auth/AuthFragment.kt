package com.example.bisit.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bisit.databinding.FragmentAuthBinding
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

        loginViewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                // 로그인 성공 시 사장님 온보딩으로 이동하도록 신호 전달
                val intent = Intent(requireContext(), SignUpActivity::class.java).apply {
                    putExtra("START_DESTINATION", "OWNER_INTRO")
                }
                startActivity(intent)
                activity?.finish()
            } else {
                // 실패 시 토스트 등 알림 처리
                Toast.makeText(context, "임시 로그인 실패: ${loginViewModel.errorMessage.value}", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}
