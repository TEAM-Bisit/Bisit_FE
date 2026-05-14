package kr.bisit.app.ui.signUp

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
import kr.bisit.app.R
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.mypage.SmsResponse
import kr.bisit.app.data.model.mypage.SmsVerifyResponse
import kr.bisit.app.data.model.todayReservation.CommonResponse
import kr.bisit.app.databinding.FragmentSignUpInfoBinding
import kr.bisit.app.ui.dialog.CommonInfoDialog
import java.util.regex.Pattern

class SignUpInfoFragment : Fragment() {

    private var _binding: FragmentSignUpInfoBinding? = null
    private val binding get() = _binding!!

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    private val viewModel: SignUpViewModel by activityViewModels()

    private val smsApi by lazy { RetrofitClient.getSmsApi(requireContext()) }

    private val authApi by lazy { RetrofitClient.getAuthApi(requireContext()) }

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
            val email = binding.etEmail.text.toString()
            checkEmailAndProceed(email)
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

            if (phone == "01012345678") { // 테스트용 코드
                viewModel.isVerificationUiVisible.value = true
                return@setOnClickListener
            }

            // 1단계: 번호 중복 체크 API 호출
            val authApi = RetrofitClient.getAuthApi(requireContext())
            authApi.checkPhoneNumber(phone).enqueue(object : retrofit2.Callback<CommonResponse<Boolean>> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResponse<Boolean>>,
                    response: retrofit2.Response<CommonResponse<Boolean>>
                ) {
                    if (response.isSuccessful) {
                        val isAvailable = response.body()?.data == true // data가 true면 가입 가능한 번호

                        if (isAvailable) {
                            // 2단계: 번호가 사용 가능하므로 SMS 발송 진행
                            sendSmsRequest(phone)
                        } else {
                            // 번호 중복 시 다이얼로그 표시
                            CommonInfoDialog(
                                message = "이미 가입된 전화번호입니다.",
                                onConfirm = { }
                            ).show(parentFragmentManager, "PhoneDuplicateDialog")
                        }
                    } else {
                        showErrorDialog("중복 체크에 실패했습니다. 다시 시도해주세요.")
                    }
                }

                override fun onFailure(call: retrofit2.Call<CommonResponse<Boolean>>, t: Throwable) {
                    showErrorDialog("네트워크 오류가 발생했습니다.")
                }
            })
        }

        binding.btnConfirmVerification.setOnClickListener {
            val phone = binding.etPhone.text.toString().replace("-", "")
            val code = binding.etVerificationCode.text.toString()

            if (phone == "01012345678" && code == "000000") { // 테스트용 코드
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
                viewModel.name = binding.etName.text.toString()
                viewModel.email = binding.etEmail.text.toString()
                viewModel.phone = binding.etPhone.text.toString().replace("-", "")
                viewModel.gender = if (binding.etGender.text.toString() == "남") "MALE" else "FEMALE"

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

    private fun sendSmsRequest(phone: String) {
        val requestBody = mapOf("phoneNumber" to phone)
        smsApi.sendSms(requestBody).enqueue(object : retrofit2.Callback<SmsResponse> {
            override fun onResponse(call: retrofit2.Call<SmsResponse>, response: retrofit2.Response<SmsResponse>) {
                if (response.isSuccessful) {
                    viewModel.isVerificationUiVisible.value = true
                } else {
                    showErrorDialog("인증번호 발송에 실패했습니다.")
                }
            }
            override fun onFailure(call: retrofit2.Call<SmsResponse>, t: Throwable) {
                showErrorDialog("네트워크 오류로 인증번호를 보낼 수 없습니다.")
            }
        })
    }

    private fun checkEmailAndProceed(email: String) {
        authApi.checkEmail(email).enqueue(object : retrofit2.Callback<CommonResponse<Boolean>> {
            override fun onResponse(
                call: retrofit2.Call<CommonResponse<Boolean>>,
                response: retrofit2.Response<CommonResponse<Boolean>>
            ) {
                if (response.isSuccessful) {
                    val isAvailable = response.body()?.data == true

                    if (isAvailable) {
                        // 중복되지 않으면 약관 시트 표시
                        showTermsSheet()
                    } else {
                        // 중복된 경우 다이얼로그 표시
                        CommonInfoDialog(
                            message = "이미 존재하는 이메일입니다.",
                            onConfirm = { }
                        ).show(parentFragmentManager, "EmailDuplicateDialog")
                    }
                } else {
                    showErrorDialog("이미 존재하는 이메일입니다.")
                }
            }

            override fun onFailure(call: retrofit2.Call<CommonResponse<Boolean>>, t: Throwable) {
                showErrorDialog("네트워크 오류가 발생했습니다.")
            }
        })
    }

    private fun showErrorDialog(message: String) {
        CommonInfoDialog(message = message, onConfirm = {}).show(parentFragmentManager, "InfoDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}