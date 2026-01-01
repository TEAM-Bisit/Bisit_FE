package com.example.bisit.ui.signUp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentStoreInfoBinding
import com.example.bisit.ui.customerPay.AddressSearchActivity
import kotlin.jvm.java

class StoreInfoFragment : Fragment() {

    private var _binding: FragmentStoreInfoBinding? = null
    private val binding get() = _binding!!

    // 연락처 포맷팅 플래그
    private var isFormattingContact = false

    private val addressLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val address = result.data?.getStringExtra("selectedAddress")
                if (!address.isNullOrBlank()) {
                    binding.etAddressMain.setText(address)
                    // 주소가 입력되면 상세주소 칸으로 포커스 이동
                    binding.etAddressDetail.requestFocus()
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. 시작 시 '다음' 버튼 비활성화
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(false)

        // 2. 주소 검색 버튼 리스너
        binding.btnSearchAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddressSearchActivity::class.java)
            addressLauncher.launch(intent)
        }

        // 3. 연락처 자동 포맷팅 및 필터 (이전과 동일 로직)
        setupContactNumberLogic()

        // 4. 모든 입력 필드에 TextWatcher 등록 -> 유효성 검사
        val validationWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkValidation()
            }
        }

        binding.etStoreName.addTextChangedListener(validationWatcher)
        binding.etStoreContact.addTextChangedListener(validationWatcher)
        binding.etAddressMain.addTextChangedListener(validationWatcher)
        binding.etAddressDetail.addTextChangedListener(validationWatcher)
    }

    /**
     * 모든 필드가 채워졌는지 확인하고 부모 프래그먼트의 '다음' 버튼 상태 변경
     */
    private fun checkValidation() {
        val name = binding.etStoreName.text.toString()
        val contact = binding.etStoreContact.text.toString()
        val addressMain = binding.etAddressMain.text.toString()
        val addressDetail = binding.etAddressDetail.text.toString()

        // (조건) 이름 있음 && 연락처 12~13자리 && 주소 있음 && 상세주소 있음
        val isValid = name.isNotBlank() &&
                contact.length >= 12 && // 02-000-0000 (11) or 010-0000-0000 (13)
                addressMain.isNotBlank() &&
                addressDetail.isNotBlank()

        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(isValid)
    }

    private fun setupContactNumberLogic() {
        // 1. 하이픈(-) 수동 입력 방지 필터 (재사용을 위해 변수로 선언)
        val blockHyphenFilter = InputFilter { source, _, _, _, _, _ ->
            if (source.toString() == "-") "" else null
        }

        // 초기 필터 설정 (기본 13자리)
        binding.etStoreContact.filters = arrayOf(blockHyphenFilter, InputFilter.LengthFilter(13))

        binding.etStoreContact.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormattingContact || s == null) return
                isFormattingContact = true

                val originalText = s.toString()
                val digits = originalText.replace("-", "")
                val formatted = StringBuilder()

                // --- [1] 자동 하이픈 로직 ---
                if (digits.startsWith("02")) {
                    // 서울 (02): 02-xxxx-xxxx (10자리+2하이픈 = 12글자)
                    if (digits.length <= 2) {
                        formatted.append(digits)
                    } else if (digits.length <= 5) {
                        formatted.append(digits.substring(0, 2)).append("-")
                            .append(digits.substring(2))
                    } else if (digits.length <= 9) {
                        formatted.append(digits.substring(0, 2)).append("-")
                            .append(digits.substring(2, 5)).append("-")
                            .append(digits.substring(5))
                    } else {
                        formatted.append(digits.substring(0, 2)).append("-")
                            .append(digits.substring(2, 6)).append("-")
                            .append(digits.substring(6))
                    }
                } else if (digits.startsWith("1")) {
                    // 대표번호 (1577): 1577-1234 (8자리+1하이픈 = 9글자)
                    if (digits.length <= 4) {
                        formatted.append(digits)
                    } else {
                        formatted.append(digits.substring(0, 4)).append("-")
                            .append(digits.substring(4))
                    }
                } else {
                    // 휴대폰/경기/인천 등: 010-xxxx-xxxx (11자리+2하이픈 = 13글자)
                    if (digits.length <= 3) {
                        formatted.append(digits)
                    } else if (digits.length <= 6) {
                        formatted.append(digits.substring(0, 3)).append("-")
                            .append(digits.substring(3))
                    } else if (digits.length <= 10) {
                        formatted.append(digits.substring(0, 3)).append("-")
                            .append(digits.substring(3, 6)).append("-")
                            .append(digits.substring(6))
                    } else {
                        formatted.append(digits.substring(0, 3)).append("-")
                            .append(digits.substring(3, 7)).append("-")
                            .append(digits.substring(7))
                    }
                }

                val newText = formatted.toString()
                if (newText != originalText) {
                    s.replace(0, s.length, newText)
                }

                // --- [2] MaxLength 동적 변경 로직 ---

                // 패턴에 따른 목표 길이 설정
                val targetMaxLength = when {
                    digits.startsWith("02") -> 12 // 02-1234-5678
                    digits.startsWith("1") -> 9   // 1577-1234
                    else -> 13                    // 010-1234-5678
                }

                // 현재 설정된 필터들을 가져와서 LengthFilter가 변경이 필요한지 확인
                val currentFilters = binding.etStoreContact.filters
                val hasCorrectLength = currentFilters.any {
                    it is InputFilter.LengthFilter && it.max == targetMaxLength
                }

                // 길이가 다를 때만 필터를 교체 (불필요한 리소스 낭비 방지)
                if (!hasCorrectLength) {
                    binding.etStoreContact.filters = arrayOf(blockHyphenFilter, InputFilter.LengthFilter(targetMaxLength))
                }

                isFormattingContact = false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreInfoFragment()
    }
}