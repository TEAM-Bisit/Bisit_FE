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
import com.example.bisit.databinding.FragmentSignUpInfoBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import java.util.regex.Pattern

class SignUpInfoFragment : Fragment() {

    private var _binding: FragmentSignUpInfoBinding? = null
    private val binding get() = _binding!!

    private val phonePattern: Pattern = Pattern.compile("^010-\\d{4}-\\d{4}$")

    private val viewModel: SignUpViewModel by activityViewModels()

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
            // TODO: 서버에 인증번호 전송 요청 (binding.etPhone.text.toString())

            viewModel.isVerificationUiVisible.value = true
        }

        binding.btnConfirmVerification.setOnClickListener {
            val dialog = CommonInfoDialog(
                message = "인증이 완료되었습니다.",
                onConfirm = {
                    viewModel.isPhoneVerified.value = true
                }
            )
            dialog.show(parentFragmentManager, "VerificationCompleteDialog")
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