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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentSignUpInfoBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.dialog.TermsAgreementSheet
import java.util.regex.Pattern

class SignUpInfoFragment : Fragment() {

    private var _binding: FragmentSignUpInfoBinding? = null
    private val binding get() = _binding!!

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    private var isPhoneVerified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpInfoBinding.inflate(inflater, container, false)
        return binding.root
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
            findNavController().navigate(R.id.action_signUpInfoFragment_to_signUpCredentialsFragment)
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
                val input = s.toString()
                val isValid = phonePattern.matcher(input).matches()
                binding.btnVerify.isEnabled = isValid
            }
        })

        binding.btnVerify.setOnClickListener {
            // TODO: 서버에 인증번호 전송 요청 (binding.etPhone.text.toString())

            binding.groupVerification.visibility = View.VISIBLE

            binding.etPhone.isEnabled = false
            binding.btnVerify.isEnabled = false
        }

        binding.btnConfirmVerification.setOnClickListener {
            val verificationCode = binding.etVerificationCode.text.toString()

            // TODO: 입력된 인증번호(verificationCode)를 서버로 보내 검증합니다.

            val dialog = CommonInfoDialog(
                message = "인증이 완료되었습니다.",
                onConfirm = {
                    binding.etVerificationCode.isEnabled = false
                    binding.btnConfirmVerification.isEnabled = false

                    isPhoneVerified = true
                    checkAllFieldsAndEnableNextButton()
                }
            )
            dialog.show(parentFragmentManager, "VerificationCompleteDialog")
        }

        binding.etVerificationCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
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

        binding.btnNext.setOnClickListener {
            // "이용약관 동의" BottomSheet를 띄웁니다.
            val sheet = TermsAgreementSheet {
                // 약관 동의가 완료되면 (BottomSheet의 "다음" 버튼 클릭 시)
                // 2단계(ID/PW 입력) 프래그먼트로 이동합니다.
                findNavController().navigate(R.id.action_signUpInfoFragment_to_signUpCredentialsFragment)
            }
            sheet.show(parentFragmentManager, "TermsAgreementSheet")
        }
    }

    private fun checkAllFieldsAndEnableNextButton() {
        val isNameValid = binding.etName.text.isNotBlank()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()

        binding.btnNext.isEnabled = isNameValid && isEmailValid && isPhoneVerified
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}