package com.example.bisit.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.example.bisit.databinding.SheetTermsAgreementBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TermsAgreementSheet(
    private val onAgreementComplete: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: SheetTermsAgreementBinding? = null
    private val binding get() = _binding!!

    private lateinit var requiredCheckBoxes: List<CheckBox>
    private lateinit var allCheckBoxes: List<CheckBox>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SheetTermsAgreementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 체크박스 리스트 초기화
        requiredCheckBoxes = listOf(binding.cbTermService, binding.cbTermLocation)
        allCheckBoxes = requiredCheckBoxes

        setupListeners()
    }

    private fun setupListeners() {
        // "전체 동의" 클릭 리스너
        binding.cbAllAgree.setOnClickListener {
            val isChecked = binding.cbAllAgree.isChecked
            allCheckBoxes.forEach { it.isChecked = isChecked }
            checkRequiredFields()
        }

        // 개별 약관 클릭 리스너
        allCheckBoxes.forEach { checkBox ->
            checkBox.setOnClickListener {
                checkAllAgreeState()
                checkRequiredFields()
            }
        }

        // "다음" 버튼 클릭 리스너
        binding.btnNext.setOnClickListener {
            onAgreementComplete.invoke()
            dismiss()
        }

        // TODO: 각 약관 화살표(>) 클릭 시 약관 상세 보기 웹뷰(또는 새 Fragment)로 이동하는 리스너 추가
        // binding.ivArrowService.setOnClickListener { ... }
        // binding.ivArrowPrivacy.setOnClickListener { ... }
        // binding.ivArrowMarketing.setOnClickListener { ... }
    }

    // 개별 약관 상태에 따라 "전체 동의" 체크박스 상태 업데이트
    private fun checkAllAgreeState() {
        binding.cbAllAgree.isChecked = allCheckBoxes.all { it.isChecked }
    }

    // (필수) 약관이 모두 동의되었는지 확인하여 "다음" 버튼 활성화
    private fun checkRequiredFields() {
        binding.btnNext.isEnabled = requiredCheckBoxes.all { it.isChecked }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}