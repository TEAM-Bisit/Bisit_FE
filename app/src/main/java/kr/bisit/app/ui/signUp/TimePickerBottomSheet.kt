package kr.bisit.app.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import kr.bisit.app.databinding.BottomSheetTimePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TimePickerBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTimePickerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = arguments?.getString("title") ?: "시간 선택"
        val requestKey = arguments?.getString("requestKey") ?: "time_picker_key"

        binding.tvTitle.text = title

        // 시간 Picker 설정 (0~23시)
        binding.hourPicker.minValue = 0
        binding.hourPicker.maxValue = 23
        binding.hourPicker.setFormatter { String.format("%02d", it) }

        // 분 Picker 설정 (5분 단위)
        val minutes = Array(12) { i -> String.format("%02d", i * 5) }
        binding.minutePicker.minValue = 0
        binding.minutePicker.maxValue = 11
        binding.minutePicker.displayedValues = minutes

        binding.btnDone.setOnClickListener {
            val hour = binding.hourPicker.value
            val minute = binding.minutePicker.value * 5
            val timeString = String.format("%02d:%02d", hour, minute)

            // 결과를 부모 Fragment로 전달
            setFragmentResult(requestKey, bundleOf("selectedTime" to timeString))
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(title: String, requestKey: String): TimePickerBottomSheet {
            return TimePickerBottomSheet().apply {
                arguments = bundleOf("title" to title, "requestKey" to requestKey)
            }
        }
    }
}