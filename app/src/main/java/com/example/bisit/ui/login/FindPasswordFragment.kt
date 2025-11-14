package com.example.bisit.ui.login

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.bisit.databinding.FragmentFindPasswordBinding // 바인딩 변경
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.dialog.CustomDialog // CustomDialog 사용 (예상)
import java.util.regex.Pattern

class FindPasswordFragment : Fragment() { // 클래스명 변경

    private var _binding: FragmentFindPasswordBinding? = null // 바인딩 변경
    private val binding get() = _binding!!

    // ViewModel 변경
    private val viewModel: FindPasswordViewModel by viewModels()

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindPasswordBinding.inflate(inflater, container, false) // 바인딩 변경
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeUiState()
    }

    private fun setupClickListeners() {
        binding.btnVerify.setOnClickListener {
            // TODO: 서버에 인증번호 전송 요청
            viewModel.isVerificationUiVisibleInput.value = true
        }

        binding.btnConfirmVerification.setOnClickListener {
            // TODO: 서버에 인증번호 확인 요청

            val dialog = CommonInfoDialog(
                message = "인증이 완료되었습니다.",
                onConfirm = {
                    viewModel.isPhoneVerifiedInput.value = true
                }
            )
            dialog.show(parentFragmentManager, "VerificationCompleteDialog")
        }

        binding.btnFindPassword.setOnClickListener {
            // TODO: 이름, 아이디, 인증된 폰번호로 비밀번호 찾기(재설정) 로직 구현

            // 1. CustomDialog를 띄웁니다.
            val dialog = CustomDialog(
                title = "해당 정보로 안내를 전송했습니다.",
                subtitle = "안내를 확인해주세요.",
                onConfirm = {
                    // 2. '닫기' 버튼을 누르면 그냥 닫히기만 합니다. (아무것도 안 함)
                }
            )
            dialog.show(parentFragmentManager, "CustomDialog") // 태그도 하나만 사용
        }
    }

    private fun setupTextWatchers() {
        // 1. 휴대폰 번호 포맷팅
        val blockHyphenFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString() == "-") "" else null
        }
        val lengthFilter = InputFilter.LengthFilter(13)
        binding.etPhone.filters = arrayOf(blockHyphenFilter, lengthFilter)
        binding.etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))

        // 2. '번호 인증' 버튼 활성화
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isVerificationUiVisible.value == true) return
                binding.btnVerify.isEnabled = phonePattern.matcher(s.toString()).matches()
            }
        })

        // 3. '완료' 버튼 활성화
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isPhoneVerified.value == true) return
                binding.btnConfirmVerification.isEnabled = s?.length == 6
            }
        })

        // 4. '이름'과 '아이디' 입력 감지 -> '비밀번호 찾기' 버튼 활성화 체크
        val buttonEnablerWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsAndEnableFindPasswordButton() // 함수명 변경
            }
        }
        binding.etName.addTextChangedListener(buttonEnablerWatcher)
        binding.etId.addTextChangedListener(buttonEnablerWatcher) // etId 리스너 추가
    }

    // ViewModel 관찰
    private fun observeUiState() {
        viewModel.isVerificationUiVisible.observe(viewLifecycleOwner) { isVisible ->
            if (isVisible) {
                binding.groupVerification.visibility = View.VISIBLE
                binding.etPhone.isEnabled = false
                binding.btnVerify.isEnabled = false
            } else {
                binding.groupVerification.visibility = View.GONE
                binding.etPhone.isEnabled = true
            }
        }

        viewModel.isPhoneVerified.observe(viewLifecycleOwner) { isVerified ->
            if (isVerified) {
                binding.etVerificationCode.isEnabled = false
                binding.btnConfirmVerification.isEnabled = false
            } else {
                binding.etVerificationCode.isEnabled = true
            }
            checkAllFieldsAndEnableFindPasswordButton() // 함수명 변경
        }
    }

    // '비밀번호 찾기' 버튼 활성화 로직
    private fun checkAllFieldsAndEnableFindPasswordButton() { // 함수명 변경
        val isNameValid = binding.etName.text.isNotBlank()
        val isIdValid = binding.etId.text.isNotBlank() // etId 유효성 체크 추가
        val isPhoneVerified = viewModel.isPhoneVerified.value ?: false

        binding.btnFindPassword.isEnabled = isNameValid && isIdValid && isPhoneVerified // 조건 추가
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}