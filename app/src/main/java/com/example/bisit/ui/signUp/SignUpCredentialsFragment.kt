package com.example.bisit.ui.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.todayReservation.CommonResponse
import com.example.bisit.databinding.FragmentSignUpCredentialsBinding
import kotlinx.coroutines.Job
import androidx.lifecycle.lifecycleScope
import com.example.bisit.data.model.signUp.SignUpRequest
import com.example.bisit.data.model.signUp.SignUpResponse
import com.example.bisit.ui.dialog.CustomDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class SignUpCredentialsFragment : Fragment() {

    private var _binding: FragmentSignUpCredentialsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

    private var isIdValid = false
    private var isPasswordValid = false
    private var isPasswordConfirmValid = false

    private val authApi by lazy { RetrofitClient.getAuthApi(requireContext()) }
    private var debounceJob: Job? = null
    private var isIdChecked = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etId.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) { // 포커스를 잃었을 때
                val loginId = binding.etId.text.toString()
                if (loginId.isNotBlank()) {
                    checkDuplicateId(loginId)
                }
            }
        }

        setupTextWatchers()
        binding.btnNext.setOnClickListener {
            performSignUp()
        }
    }

    private fun setupTextWatchers() {
        binding.etId.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                isIdChecked = false
                isIdValid = false
                checkAllFieldsAndEnableNextButton()

                debounceJob?.cancel()

                if (input.isBlank()) {
                    binding.layoutId.error = null
                    binding.layoutId.helperText = null
                    return
                }

                debounceJob = viewLifecycleOwner.lifecycleScope.launch {
                    delay(500L)
                    checkDuplicateId(input)
                }
            }
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                isPasswordValid = s.toString().isNotBlank()
                binding.layoutPassword.helperText = " "

                validatePasswordConfirm()
                checkAllFieldsAndEnableNextButton()
            }
        })

        binding.etPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordConfirm()
                checkAllFieldsAndEnableNextButton()
            }
        })
    }

    private fun validatePasswordConfirm() {
        val password = binding.etPassword.text.toString()
        val passwordConfirm = binding.etPasswordConfirm.text.toString()
        val context = requireContext()

        if (passwordConfirm.isBlank()) {
            isPasswordConfirmValid = false
            binding.layoutPasswordConfirm.error = null
            binding.layoutPasswordConfirm.helperText = " "

        } else if (password == passwordConfirm) {
            isPasswordConfirmValid = true
            binding.layoutPasswordConfirm.error = null
            binding.layoutPasswordConfirm.helperText = "비밀번호가 일치합니다."
            binding.layoutPasswordConfirm.setHelperTextColor(
                ContextCompat.getColorStateList(context, R.color.green)!!
            )
        } else {
            isPasswordConfirmValid = false
            binding.layoutPasswordConfirm.helperText = null
            binding.layoutPasswordConfirm.error = "비밀번호가 일치하지 않습니다."
        }
    }

    private fun checkDuplicateId(loginId: String) {
        authApi.checkLoginId(loginId).enqueue(object : retrofit2.Callback<CommonResponse<Boolean>> {
            override fun onResponse(
                call: retrofit2.Call<CommonResponse<Boolean>>,
                response: retrofit2.Response<CommonResponse<Boolean>>
            ) {
                if (response.isSuccessful) {
                    val isAvailable = response.body()?.data == true

                    if (isAvailable) {
                        isIdValid = true
                        isIdChecked = true
                        binding.layoutId.helperText = "사용 가능한 아이디입니다."
                        binding.layoutId.error = null
                        binding.layoutId.setHelperTextColor(
                            ContextCompat.getColorStateList(requireContext(), R.color.green)!!
                        )
                    } else {
                        isIdValid = false
                        isIdChecked = false
                        binding.layoutId.error = "이미 사용 중인 아이디입니다."
                    }
                }
                checkAllFieldsAndEnableNextButton()
            }

            override fun onFailure(call: retrofit2.Call<CommonResponse<Boolean>>, t: Throwable) {
                binding.layoutId.error = "네트워크 오류가 발생했습니다."
            }
        })
    }

    private fun checkAllFieldsAndEnableNextButton() {
        binding.btnNext.isEnabled = isIdValid && isPasswordValid && isPasswordConfirmValid
    }

    private fun performSignUp() {
        Log.d("SignUpDebug", "name: ${viewModel.name}")
        Log.d("SignUpDebug", "email: ${viewModel.email}")
        Log.d("SignUpDebug", "phone: ${viewModel.phone}")
        Log.d("SignUpDebug", "gender: ${viewModel.gender}")
        Log.d("SignUpDebug", "loginId: ${binding.etId.text}")

        // 1. 전체 요청 객체 생성 (ViewModel + 현재 입력값)
        val request = SignUpRequest(
            name = viewModel.name,
            email = viewModel.email,
            phone = viewModel.phone,
            gender = viewModel.gender,
            loginId = binding.etId.text.toString(),
            password = binding.etPassword.text.toString(),
            confirmPassword = binding.etPasswordConfirm.text.toString()
        )

        // 2. 서버 전송
        authApi.signUp(request).enqueue(object : retrofit2.Callback<SignUpResponse> {
            override fun onResponse(call: Call<SignUpResponse>, response: Response<SignUpResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    // 성공 시 완료 화면 이동
                    findNavController().navigate(R.id.action_signUpCredentialsFragment_to_signUpCompleteFragment)
                } else {
                    // [중요] 서버가 보내는 진짜 에러 이유를 확인하는 코드
                    val errorBody = response.errorBody()?.string()
                    Log.e("SignUpError", "서버가 보낸 에러 내용: $errorBody")

                    // 에러 팝업 띄우기
                    val dialog = CustomDialog(
                        title = "회원가입 실패",
                        subtitle = "입력 정보를 다시 확인해주세요."
                    )
                    dialog.show(parentFragmentManager, "SignUpError")
                }
            }

            override fun onFailure(call: Call<SignUpResponse>, t: Throwable) {
                com.example.bisit.ui.dialog.CustomDialog(
                    title = "네트워크 오류",
                    subtitle = "서버와의 통신이 원활하지 않습니다."
                ).show(parentFragmentManager, "NetworkError")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}