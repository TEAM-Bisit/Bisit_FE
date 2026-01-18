package com.example.bisit.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bisit.R
import com.example.bisit.databinding.FragmentFindPasswordBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.dialog.CustomDialog
import java.util.regex.Pattern

class FindPasswordFragment : Fragment() {

    private var _binding: FragmentFindPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FindPasswordViewModel by viewModels()

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeUiState()
    }

    private fun setupClickListeners() {
        // 1. 번호 인증 요청
        binding.btnVerify.setOnClickListener {
            // TODO: 서버에 인증번호 전송 API 호출
            viewModel.isVerificationUiVisibleInput.value = true
        }

        // 2. 인증번호 확인 버튼
        binding.btnConfirmVerification.setOnClickListener {
            // TODO: 서버에 인증번호 확인 API 호출
            val dialog = CommonInfoDialog(
                message = "인증이 완료되었습니다.\n새로운 비밀번호를 입력해주세요.",
                onConfirm = {
                    viewModel.isPhoneVerifiedInput.value = true
                    // 인증 성공 시 재설정 그룹 표시
                    binding.groupResetPassword.visibility = View.VISIBLE
                }
            )
            dialog.show(parentFragmentManager, "VerificationCompleteDialog")
        }

        // 3. 최종 비밀번호 재설정 완료 버튼
        binding.btnFindPassword.setOnClickListener {
            // TODO: 비밀번호 재설정 API 호출 (이름, 아이디, 폰번호, 새비밀번호 전송)
            val dialog = CustomDialog(
                title = "비밀번호 재설정 완료",
                subtitle = "새로운 비밀번호로 로그인해주세요.",
                onConfirm = {
                },
                onDismiss = {
                    if (isAdded) {
                        parentFragmentManager.popBackStack()
                    }
                }
            )
            dialog.show(parentFragmentManager, "ResetSuccessDialog")
        }
    }

    private fun setupTextWatchers() {
        // 휴대폰 번호 포맷팅 및 필터
        val blockHyphenFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString() == "-") "" else null
        }
        binding.etPhone.filters = arrayOf(blockHyphenFilter, InputFilter.LengthFilter(13))
        binding.etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))

        // 모든 입력창 감지하여 하단 버튼 활성화 체크
        val commonWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsAndEnableFindPasswordButton()
            }
        }

        // 각 EditText에 리스너 등록
        binding.etName.addTextChangedListener(commonWatcher)
        binding.etId.addTextChangedListener(commonWatcher)
        binding.etNewPassword.addTextChangedListener(commonWatcher)
        binding.etConfirmPassword.addTextChangedListener(commonWatcher)

        // '번호 인증' 버튼 활성화 체크
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isVerificationUiVisible.value == true) return
                binding.btnVerify.isEnabled = phonePattern.matcher(s.toString()).matches()
            }
        })

        // '완료' 버튼 활성화 체크
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isPhoneVerified.value == true) return
                binding.btnConfirmVerification.isEnabled = s?.length == 6
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun observeUiState() {
        // 인증번호 입력창 노출 여부
        viewModel.isVerificationUiVisible.observe(viewLifecycleOwner) { isVisible ->
            binding.groupVerification.visibility = if (isVisible) View.VISIBLE else View.GONE
            if (isVisible) {
                binding.etPhone.isEnabled = false
                binding.btnVerify.isEnabled = false
            }
        }

        // 휴대폰 인증 완료 여부
        viewModel.isPhoneVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                binding.etVerificationCode.isEnabled = false
                binding.btnConfirmVerification.isEnabled = false
                binding.groupResetPassword.visibility = View.VISIBLE // 재설정 UI 표시
            }
            checkAllFieldsAndEnableFindPasswordButton()
        }
    }

    // 최종 '비밀번호 재설정' 버튼 활성화 로직
    private fun checkAllFieldsAndEnableFindPasswordButton() {
        val name = binding.etName.text.toString()
        val id = binding.etId.text.toString()
        val newPassword = binding.etNewPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val isPhoneVerified = viewModel.isPhoneVerified.value ?: false

        // 1. 공백 체크
        val isNotEmpty = name.isNotBlank() && id.isNotBlank() &&
                newPassword.isNotBlank() && confirmPassword.isNotBlank()

        // 2. 비밀번호 일치 여부 및 에러 메시지 표시 (SignUpCredentialsFragment 방식)
        val isPasswordMatched = if (confirmPassword.isBlank()) {
            binding.layoutConfirmPassword.error = null
            binding.layoutConfirmPassword.helperText = " "
            false
        } else if (newPassword == confirmPassword) {
            binding.layoutConfirmPassword.error = null
            binding.layoutConfirmPassword.helperText = "비밀번호가 일치합니다."
            binding.layoutConfirmPassword.setHelperTextColor(
                ContextCompat.getColorStateList(requireContext(), R.color.green)!!
            )
            true
        } else {
            binding.layoutConfirmPassword.helperText = null
            binding.layoutConfirmPassword.error = "비밀번호가 일치하지 않습니다."
            false
        }

        // 3. 최종 버튼 활성화
        binding.btnFindPassword.isEnabled = isNotEmpty && isPasswordMatched && isPhoneVerified
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}