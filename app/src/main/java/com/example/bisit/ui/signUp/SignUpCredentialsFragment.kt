package com.example.bisit.ui.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            // ... (API 호출 등) ...
            findNavController().navigate(R.id.action_signUpCredentialsFragment_to_signUpCompleteFragment)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}