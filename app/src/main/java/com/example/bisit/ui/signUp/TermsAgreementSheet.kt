package com.example.bisit.ui.signUp // ✨ 1. 패키지 경로를 ui.signUp으로 수정

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.activityViewModels
import com.example.bisit.databinding.SheetTermsAgreementBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

enum class TermType {
    SERVICE, LOCATION
}

class TermsAgreementSheet(
    private val onAgreementComplete: () -> Unit,
    private val onTermClick: (TermType) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: SheetTermsAgreementBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by activityViewModels()

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

        requiredCheckBoxes = listOf(binding.cbTermService, binding.cbTermLocation)
        allCheckBoxes = requiredCheckBoxes

        binding.cbTermService.isChecked = viewModel.isTermServiceChecked.value ?: false
        binding.cbTermLocation.isChecked = viewModel.isTermLocationChecked.value ?: false
        binding.cbAllAgree.isChecked = viewModel.isAllChecked.value ?: false
        checkRequiredFields()
        setupListeners()
    }

    private fun setupListeners() {
        binding.cbAllAgree.setOnClickListener {
            val isChecked = binding.cbAllAgree.isChecked
            allCheckBoxes.forEach { it.isChecked = isChecked }

            viewModel.isAllChecked.value = isChecked
            viewModel.isTermServiceChecked.value = isChecked
            viewModel.isTermLocationChecked.value = isChecked

            checkRequiredFields()
        }

        allCheckBoxes.forEach { checkBox ->
            checkBox.setOnClickListener {
                checkAllAgreeState()
                checkRequiredFields()

                viewModel.isTermServiceChecked.value = binding.cbTermService.isChecked
                viewModel.isTermLocationChecked.value = binding.cbTermLocation.isChecked
            }
        }

        binding.btnNext.setOnClickListener {
            onAgreementComplete.invoke()
            dismiss()
        }

        binding.ivArrowService.setOnClickListener {
            onTermClick.invoke(TermType.SERVICE)
            dismiss()
        }

        binding.ivArrowLocation.setOnClickListener {
            onTermClick.invoke(TermType.LOCATION)
            dismiss()
        }
    }

    private fun checkAllAgreeState() {
        val isAllChecked = allCheckBoxes.all { it.isChecked }
        binding.cbAllAgree.isChecked = isAllChecked
        viewModel.isAllChecked.value = isAllChecked
    }

    private fun checkRequiredFields() {
        binding.btnNext.isEnabled = requiredCheckBoxes.all { it.isChecked }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}