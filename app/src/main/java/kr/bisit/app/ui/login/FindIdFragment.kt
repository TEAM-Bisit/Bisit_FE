package kr.bisit.app.ui.login

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
import kr.bisit.app.databinding.FragmentFindIdBinding
import kr.bisit.app.ui.dialog.CommonInfoDialog
import kr.bisit.app.ui.dialog.CustomDialog
import kr.bisit.app.ui.dialog.FindIdDialog
import java.util.regex.Pattern

class FindIdFragment : Fragment() {

    private var _binding: FragmentFindIdBinding? = null
    private val binding get() = _binding!!

    // ViewModel 인스턴스 생성
    private val viewModel: FindIdViewModel by viewModels()

    // SignUp에서 가져온 전화번호 패턴
    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFindIdBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeUiState() // ViewModel 관찰 시작
    }

    private fun setupClickListeners() {
        binding.btnVerify.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            if (phone.isNotBlank()) {
                viewModel.sendVerificationCode(requireContext(), phone)
            }
        }

        binding.btnConfirmVerification.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            val code = binding.etVerificationCode.text.toString()
            if (code.length == 6) {
                viewModel.verifyCode(requireContext(), phone, code)
            }
        }

        binding.btnFindId.setOnClickListener {
            val name = binding.etName.text.toString()
            val rawPhone = binding.etPhone.text.toString()

            val cleanPhone = rawPhone.replace("-", "")

            viewModel.findId(requireContext(), name, cleanPhone)
        }
    }

    private fun setupTextWatchers() {
        // --- SignUpInfoFragment 로직 적용 ---

        // 1. 휴대폰 번호 자동 하이픈 (-) 추가
        val blockHyphenFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString() == "-") "" else null
        }
        val lengthFilter = InputFilter.LengthFilter(13)
        binding.etPhone.filters = arrayOf(blockHyphenFilter, lengthFilter)
        binding.etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))

        // 2. 휴대폰 번호 유효성 검사 -> '번호 인증' 버튼 활성화
        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isVerificationUiVisible.value == true) return // 인증 시작되면 비활성화

                val input = s.toString()
                binding.btnVerify.isEnabled = phonePattern.matcher(input).matches()
            }
        })

        // 3. 인증번호 입력 검사 -> '완료' 버튼 활성화
        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isPhoneVerified.value == true) return // 인증 완료되면 비활성화

                binding.btnConfirmVerification.isEnabled = s?.length == 6
            }
        })

        // 4. '이름' 입력 감지 -> '아이디 찾기' 버튼 활성화 체크
        binding.etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsAndEnableFindIdButton()
            }
        })
    }

    // ViewModel의 UI 상태 변경을 관찰하여 UI 업데이트
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
            // 인증 상태가 변경되었으므로, '아이디 찾기' 버튼 상태 갱신
            checkAllFieldsAndEnableFindIdButton()
        }

        // 아이디 찾기 성공 시 결과 다이얼로그 표시
        viewModel.foundId.observe(viewLifecycleOwner) { id ->
            id?.let {
                val name = binding.etName.text.toString().ifBlank { "사용자" }
                val dialog = FindIdDialog(
                    name = name,
                    foundId = it,
                    onDismissCallback = {

                    }
                )
                dialog.show(parentFragmentManager, "FindIdDialog")
            }
        }

        // 에러 발생 시 토스트 또는 안내 표시
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                CommonInfoDialog(
                    message = it.toString(),
                    onConfirm = {
                    }
                ).show(parentFragmentManager, "ErrorDialog")
            }
        }
    }

    // '아이디 찾기' 버튼 활성화 로직
    private fun checkAllFieldsAndEnableFindIdButton() {
        val isNameValid = binding.etName.text.isNotBlank()
        val isPhoneVerified = viewModel.isPhoneVerified.value ?: false

        binding.btnFindId.isEnabled = isNameValid && isPhoneVerified
    }

    private fun showCustomMessageDialog() {
        val dialog = CustomDialog(
            title = "해당 정보로 안내를 전송했습니다.",
            subtitle = "안내를 확인해주세요.",
            onConfirm = {
                // 닫기 버튼 누르면 다이얼로그만 닫힘 (아무것도 안 함)
            }
        )
        // 태그 이름이 겹치지 않도록 주의
        dialog.show(parentFragmentManager, "MessageSentDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}