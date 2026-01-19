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
import androidx.fragment.app.activityViewModels
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.BusinessDetailValidateRequest
import com.example.bisit.data.model.shop.BusinessDetailValidateResponse
import com.example.bisit.data.model.shop.BusinessValidateRequest
import com.example.bisit.data.model.shop.BusinessValidateResponse
import com.example.bisit.data.model.staffManage.ApiResponse
import com.example.bisit.data.model.staffManage.StaffEnrollRequest
import com.example.bisit.data.model.staffManage.StaffResponse
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

    private val signUpViewModel: SignUpViewModel by activityViewModels()

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

        binding.etBusinessName.setOnEditorActionListener { textView, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE && textView.text.isNotEmpty()) {
                binding.layoutOpeningDate.visibility = View.VISIBLE // 개업 일자 레이아웃 노출
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
            val ownerName = binding.etOwnerName.text.toString().trim()
            val businessName = binding.etBusinessName.text.toString().trim()
            val openDate = binding.etOpeningDate.text.toString().trim()
            val businessNo = binding.etBusinessNumber.text.toString().trim()

            // 1. [테스트 모드] 하드코딩된 정보와 일치하는지 먼저 확인
            if (ownerName == "김사장" && businessName == "김사장" && openDate == "2026-01-01" && businessNo == "000-00-00000") {

                // 테스트 통과 시 번호 저장 및 다음 버튼 활성화
                signUpViewModel.setBusinessRegNo(businessNo.replace("-", ""))
                (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(true)

                com.example.bisit.ui.dialog.CommonInfoDialog(
                    message = "사업자 인증에 성공했습니다.\n다음 단계로 진행해주세요. (테스트 모드)",
                    onConfirm = {}
                ).show(parentFragmentManager, "TestSuccessDialog")

            } else {
                // 2. [실제 모드] 테스트 정보가 아니라면 실제 API 검증 시작
                // 하이픈을 제거하고 서버로 보냅니다.
                validateBusinessNumber(businessNo.replace("-", ""))
            }
        }
    }

    private fun validateBusinessNumber(number: String) {
        val storeApi = RetrofitClient.getStoreApi(requireContext())
        val request = BusinessValidateRequest(businessRegNo = number)

        storeApi.validateBusiness(request).enqueue(object : Callback<BusinessValidateResponse> {
            override fun onResponse(call: Call<BusinessValidateResponse>, response: Response<BusinessValidateResponse>) {
                val result = response.body()?.data
                if (response.isSuccessful && result != null) {
                    if (result.isValid) {
                        // 1. 중복되지 않은 번호 -> 상세 정보 진위 확인 시작
                        validateBusinessDetail(number)
                    } else {
                        // 2. 이미 등록된 매장 -> shopId를 넘겨서 다이얼로그 표시
                        showStaffRedirectDialog(result.shopId, number)
                    }
                } else {
                    showDialog("서버 응답 오류 (1단계)")
                }
            }

            override fun onFailure(call: Call<BusinessValidateResponse>, t: Throwable) {
                showDialog("네트워크 연결 실패 (1단계)")
            }
        })
    }

    private fun validateBusinessDetail(businessNo: String) {
        val storeApi = RetrofitClient.getStoreApi(requireContext())

        val ownerName = binding.etOwnerName.text.toString().trim()
        val businessName = binding.etBusinessName.text.toString().trim()
        val openDate = binding.etOpeningDate.text.toString().replace("-", "").trim()

        // 국세청 API 호출을 위한 모델 생성 (businessName은 임시로 대표자명 사용)
        val detailRequest = BusinessDetailValidateRequest(
            businessRegNo = businessNo,
            representativeName = ownerName,
            openDate = openDate,
            businessName = businessName
        )

        storeApi.validateDetail(detailRequest).enqueue(object : Callback<BusinessDetailValidateResponse> {
            override fun onResponse(call: Call<BusinessDetailValidateResponse>, response: Response<BusinessDetailValidateResponse>) {
                if (response.isSuccessful && response.body()?.data == true) {

                    // ★ 중요: 실제 API 인증 성공 시에도 ViewModel에 번호 저장!
                    signUpViewModel.setBusinessRegNo(businessNo)

                    (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(true)
                    showDialog("사업자 인증에 성공했습니다.\n다음 단계로 진행해주세요.")
                } else {
                    // 정보 불일치 시
                    showDialog("입력하신 정보가 국세청 등록 정보와 일치하지 않습니다.")
                }
            }

            override fun onFailure(call: Call<BusinessDetailValidateResponse>, t: Throwable) {
                showDialog("네트워크 연결 실패 (상세 검증)")
            }
        })
    }

    private fun showStaffRedirectDialog(shopId: Long, businessNo: String) {
        CustomTwoButtonDialog(
            title = "등록되어 있는 매장입니다.",
            subtitle = "직원으로 신청하시겠어요?",
            positiveButtonText = "등록하기",
            negativeButtonText = "닫기",
            onPositiveClick = {
                // 실제 직원 신청 API 호출
                requestStaffEnrollment(shopId, businessNo)
            }
        ).show(parentFragmentManager, "StaffRedirectDialog")
    }

    private fun requestStaffEnrollment(shopId: Long, businessNo: String) {
        val staffApi = RetrofitClient.getStaffManageApi(requireContext())
        val request = StaffEnrollRequest(businessRegNo = businessNo)

        staffApi.enrollStaff(shopId, request).enqueue(object : Callback<ApiResponse<StaffResponse>> {
            override fun onResponse(
                call: Call<ApiResponse<StaffResponse>>,
                response: Response<ApiResponse<StaffResponse>>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    showDialog("직원 신청이 완료되었습니다.\n사장의 승인을 기다려주세요.")
                } else {
                    showDialog("직원 신청에 실패했습니다. 다시 시도해주세요.")
                }
            }

            override fun onFailure(call: Call<ApiResponse<StaffResponse>>, t: Throwable) {
                showDialog("네트워크 연결 실패 (직원 신청)")
            }
        })
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