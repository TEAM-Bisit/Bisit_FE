package com.example.bisit.ui.signUp

import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.mypage.SmsResponse
import com.example.bisit.data.model.mypage.SmsVerifyResponse
import com.example.bisit.databinding.FragmentSignUpInfoBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import java.util.regex.Pattern

class SignUpInfoFragment : Fragment() {

    private var _binding: FragmentSignUpInfoBinding? = null
    private val binding get() = _binding!!

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    private val viewModel: SignUpViewModel by activityViewModels()

    private val smsApi by lazy { RetrofitClient.getSmsApi(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.shouldShowTermsSheetOnReturn) {
            viewModel.shouldShowTermsSheetOnReturn = false
            showTermsSheet()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val genders = resources.getStringArray(R.array.gender_array)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.etGender.setAdapter(adapter)
        binding.layoutGender.setEndIconOnClickListener {
            binding.etGender.showDropDown()
        }

        binding.btnNext.setOnClickListener {
            showTermsSheet()
        }

        val blockHyphenFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString() == "-") {
                return@InputFilter ""
            }
            null
        }
        val lengthFilter = InputFilter.LengthFilter(13)
        binding.etPhone.filters = arrayOf(blockHyphenFilter, lengthFilter)
        binding.etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher("KR"))

        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isVerificationUiVisible.value == true) return

                val input = s.toString()
                val isValid = phonePattern.matcher(input).matches()
                binding.btnVerify.isEnabled = isValid
            }
        })

        binding.btnVerify.setOnClickListener {
            val phone = binding.etPhone.text.toString().replace("-", "")

            if (phone == "01000000000") { // 테스트용 코드
                viewModel.isVerificationUiVisible.value = true
                return@setOnClickListener
            }

            val requestBody = mapOf("phoneNumber" to phone)

            smsApi.sendSms(requestBody).enqueue(object : retrofit2.Callback<SmsResponse> {
                override fun onResponse(call: retrofit2.Call<SmsResponse>, response: retrofit2.Response<SmsResponse>) {
                    if (response.isSuccessful) {
                        // 성공 시 인증번호 입력 UI 노출
                        viewModel.isVerificationUiVisible.value = true
                    } else {
                        // 실패 시 메시지 처리
                    }
                }
                override fun onFailure(call: retrofit2.Call<SmsResponse>, t: Throwable) {
                    // 네트워크 에러 처리
                }
            })
        }

        binding.btnConfirmVerification.setOnClickListener {
            val phone = binding.etPhone.text.toString().replace("-", "")
            val code = binding.etVerificationCode.text.toString()

            if (phone == "01000000000" && code == "000000") { // 테스트용 코드
                val dialog = CommonInfoDialog(
                    message = "인증이 완료되었습니다.",
                    onConfirm = {
                        viewModel.isPhoneVerified.value = true
                    }
                )
                dialog.show(parentFragmentManager, "VerificationCompleteDialog")
                return@setOnClickListener
            }

            val requestBody = mapOf(
                "phoneNumber" to phone,
                "code" to code
            )

            smsApi.verifySms(requestBody).enqueue(object : retrofit2.Callback<SmsVerifyResponse> {
                override fun onResponse(call: retrofit2.Call<SmsVerifyResponse>, response: retrofit2.Response<SmsVerifyResponse>) {
                    // 1. 서버 응답이 성공(200 OK)이고
                    // 2. data 객체 내부의 'verified' 필드가 true인지 확인합니다.
                    val isVerified = response.isSuccessful && response.body()?.data?.verified == true
                    if (isVerified) {
                        val dialog = CommonInfoDialog(
                            message = "인증이 완료되었습니다.",
                            onConfirm = {
                                viewModel.isPhoneVerified.value = true
                            }
                        )
                        dialog.show(parentFragmentManager, "VerificationCompleteDialog")
                    } else {
                        // 인증 실패 시 메시지 표시 (서버에서 보내준 message가 있다면 그것을 사용)
                        val serverMessage = response.body()?.message ?: "인증번호가 일치하지 않습니다."
                        val errorDialog = CommonInfoDialog(
                            message = serverMessage,
                            onConfirm = { }
                        )
                        errorDialog.show(parentFragmentManager, "VerificationErrorDialog")
                    }
                }

                override fun onFailure(call: retrofit2.Call<SmsVerifyResponse>, t: Throwable) {
                    // 네트워크 연결 실패 등 물리적 오류 처리
                }
            })
        }

        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (viewModel.isPhoneVerified.value == true) return

                val isValidCode = s?.length == 6
                binding.btnConfirmVerification.isEnabled = isValidCode
            }
        })

        val nextButtonEnablerWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkAllFieldsAndEnableNextButton()
            }
        }
        binding.etName.addTextChangedListener(nextButtonEnablerWatcher)
        binding.etEmail.addTextChangedListener(nextButtonEnablerWatcher)

        observeUiState()
    }

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
            checkAllFieldsAndEnableNextButton()
        }
    }

    private fun showTermsSheet() {
        val sheet = TermsAgreementSheet(
            onAgreementComplete = {
                viewModel.shouldShowTermsSheetOnReturn = false
                findNavController().navigate(R.id.action_signUpInfoFragment_to_signUpCredentialsFragment)
            },
            onTermClick = { termType ->
                viewModel.shouldShowTermsSheetOnReturn = true
                showTermsDetail(termType)
            }
        )
        sheet.show(parentFragmentManager, "TermsAgreementSheet")
    }

    private fun showTermsDetail(termType: TermType) {
        val (title, content) = when (termType) {
            TermType.SERVICE -> {
                getString(R.string.terms_title_service) to getString(R.string.terms_content_service)
            }
            TermType.LOCATION -> {
                getString(R.string.terms_title_location) to getString(R.string.terms_content_location)
            }
        }

        val bundle = bundleOf(
            "termTitle" to title,
            "termContent" to content
        )
        findNavController().navigate(R.id.action_signUpInfoFragment_to_termsDetailFragment, bundle)
    }

    private fun checkAllFieldsAndEnableNextButton() {
        val isNameValid = binding.etName.text.isNotBlank()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()

        val isPhoneVerified = viewModel.isPhoneVerified.value ?: false

        binding.btnNext.isEnabled = isNameValid && isEmailValid && isPhoneVerified
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}