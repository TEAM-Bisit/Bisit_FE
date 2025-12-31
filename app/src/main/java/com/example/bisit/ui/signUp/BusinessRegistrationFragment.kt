package com.example.bisit.ui.signUp

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.BusinessValidateRequest
import com.example.bisit.data.model.shop.BusinessValidateResponse
import com.example.bisit.databinding.FragmentBusinessRegistrationBinding
import com.example.bisit.ui.dialog.CommonInfoDialog
import com.example.bisit.ui.dialog.CustomTwoButtonDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.regex.Pattern

class BusinessRegistrationFragment : Fragment() {

    private var _binding: FragmentBusinessRegistrationBinding? = null
    private val binding get() = _binding!!

    private var isFormattingBusinessNumber = false
    private var isFormattingDate = false

    private val businessNumberPattern: Pattern = Pattern.compile("^\\d{3}-\\d{2}-\\d{5}$")
    private val datePattern: Pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBusinessRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(false)

        binding.etOwnerName.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && textView.text.isNotEmpty()) {
                binding.layoutOpeningDate.visibility = View.VISIBLE
                binding.etOpeningDate.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        val blockHyphenFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.toString() == "-") {
                return@InputFilter ""
            }
            null
        }
        binding.etOpeningDate.filters = arrayOf(blockHyphenFilter, InputFilter.LengthFilter(10))

        binding.etOpeningDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormattingDate || s == null) return

                isFormattingDate = true

                val originalText = s.toString()
                val digitsOnly = originalText.replace("-", "")
                val formatted = StringBuilder()

                for (i in digitsOnly.indices) {
                    if (i == 4) {
                        formatted.append("-")
                    }
                    if (i == 6) {
                        formatted.append("-")
                    }
                    formatted.append(digitsOnly[i])
                }

                val newText = formatted.toString()
                if (newText != originalText) {
                    s.replace(0, s.length, newText)
                }

                isFormattingDate = false
            }
        })

        binding.etOpeningDate.setOnEditorActionListener { textView, actionId, event ->
            val input = textView.text.toString()
            val isValid = datePattern.matcher(input).matches()

            if (actionId == EditorInfo.IME_ACTION_DONE && isValid) {
                binding.layoutBusinessNumber.visibility = View.VISIBLE
                binding.etBusinessNumber.requestFocus()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        binding.etBusinessNumber.filters = arrayOf(blockHyphenFilter, InputFilter.LengthFilter(12))

        binding.etBusinessNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormattingBusinessNumber || s == null) return

                isFormattingBusinessNumber = true

                val originalText = s.toString()
                val digitsOnly = originalText.replace("-", "")
                val formatted = StringBuilder()

                for (i in digitsOnly.indices) {
                    if (i == 3) {
                        formatted.append("-")
                    }
                    if (i == 5) {
                        formatted.append("-")
                    }
                    formatted.append(digitsOnly[i])
                }

                val newText = formatted.toString()
                if (newText != originalText) {
                    s.replace(0, s.length, newText)
                }

                val isValid = businessNumberPattern.matcher(newText).matches()
                binding.btnCheckBusiness.isEnabled = isValid

                isFormattingBusinessNumber = false
            }
        })

        binding.btnCheckBusiness.setOnClickListener {
            val businessNo = binding.etBusinessNumber.text.toString()
            validateBusinessNumber(businessNo)
        }
    }

    private fun validateBusinessNumber(number: String) {
        val ownerApi = RetrofitClient.getOwnerApi(requireContext())
        val request = BusinessValidateRequest(businessRegNo = number)

        ownerApi.validateBusiness(request).enqueue(object : Callback<BusinessValidateResponse> {
            override fun onResponse(call: Call<BusinessValidateResponse>, response: Response<BusinessValidateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val isAvailable = response.body()!!.data

                    if (isAvailable) {
                        // 1. 중복 아님 -> 사장님 온보딩 계속 진행
                        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(true)
                        showDialog("인증에 성공했습니다. 다음 단계로 진행해주세요.")
                    } else {
                        // 2. 중복됨 -> 직원 등록 플로우로 유도
                        showStaffRedirectDialog()
                    }
                } else {
                    showDialog("사업자 번호 확인에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<BusinessValidateResponse>, t: Throwable) {
                showDialog("네트워크 연결을 확인해주세요.")
            }
        })
    }

    private fun showStaffRedirectDialog() {
        CustomTwoButtonDialog(
            title = "등록되어 있는 매장입니다.",
            subtitle = "직원으로 신청하시겠어요?",
            positiveButtonText = "등록하기",
            negativeButtonText = "닫기",
            onPositiveClick = {
                // TODO: 직원 등록 프래그먼트 또는 액티비티로 이동하는 네비게이션 로직 구현
                // 예: findNavController().navigate(R.id.action_to_staffRegistration)
            }
        ).show(parentFragmentManager, "StaffRedirectDialog")
    }

    private fun showDialog(msg: String) {
        CommonInfoDialog(message = msg, onConfirm = {}).show(parentFragmentManager, "InfoDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = BusinessRegistrationFragment()
    }
}