package com.example.bisit.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentStoreHoursBinding

class StoreHoursFragment : Fragment() {

    private var _binding: FragmentStoreHoursBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 설정: 다음 단계 버튼 활성화 (기획에 따라 비활성 상태로 시작할 수도 있음)
        updateNextButtonState(false)

        setupListeners()
        setupResultListeners()
    }

    private fun setupListeners() {
        // 1. 정기휴무일 여부 선택 로직
        binding.btnHasHoliday.setOnClickListener {
            it.isSelected = true
            binding.btnNoHoliday.isSelected = false
            // "휴무일이 있어요" 클릭 시 요일 선택 레이아웃 노출
            binding.layoutHolidaySelection.visibility = View.VISIBLE
            validateInputs()
        }

        binding.btnNoHoliday.setOnClickListener {
            it.isSelected = true
            binding.btnHasHoliday.isSelected = false
            // "휴무일이 없어요" 클릭 시 요일 선택 레이아웃 숨김
            binding.layoutHolidaySelection.visibility = View.GONE
            validateInputs()
        }

        // 2. 요일 버튼들 토글(Toggle) 로직
        setupDayButtons()

        // 3. 브레이크타임 여부 선택 로직
        binding.btnHasBreak.setOnClickListener {
            it.isSelected = true
            binding.btnNoBreak.isSelected = false
            validateInputs()
        }

        binding.btnNoBreak.setOnClickListener {
            it.isSelected = true
            binding.btnHasBreak.isSelected = false
            validateInputs()
        }

        // 4. 시간 선택 박스 클릭 (추후 TimePickerDialog 연결)
        binding.tvOperatingTime.setOnClickListener {
            // TODO: 영업시간 선택 다이얼로그 호출
        }

        binding.tvBreakTime.setOnClickListener {
            // TODO: 브레이크타임 선택 다이얼로그 호출
        }

        binding.tvHolidayCycle.setOnClickListener { view ->
            // 1. 팝업 메뉴 생성 (Anchor를 클릭한 뷰로 설정)
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)

            // 2. 메뉴 아이템 추가
            popup.menu.add("매주")
            popup.menu.add("격주")

            // 3. 아이템 클릭 리스너 설정
            popup.setOnMenuItemClickListener { item ->
                // 선택한 텍스트로 변경
                binding.tvHolidayCycleText.text = item.title
                validateInputs()
                true
            }

            // 4. 메뉴 보이기
            popup.show()
        }

        binding.btnHasBreak.setOnClickListener {
            it.isSelected = true
            binding.btnNoBreak.isSelected = false

            // "있음" 선택 시 브레이크타임 설정 칸 보이기
            binding.layoutBreakTimeSetting.visibility = View.VISIBLE
            validateInputs()
        }

        binding.btnNoBreak.setOnClickListener {
            it.isSelected = true
            binding.btnHasBreak.isSelected = false

            // "없음" 선택 시 브레이크타임 설정 칸 숨기기
            binding.layoutBreakTimeSetting.visibility = View.GONE
            validateInputs()
        }

        // 영업시간 클릭 시: 시작 시간 선택 바텀시트 띄움
        binding.tvOperatingTime.setOnClickListener {
            showPicker("영업 시작 시간 선택", "operating_start")
        }

        // 브레이크타임 클릭 시
        binding.tvBreakTime.setOnClickListener {
            showPicker("브레이크타임 시작 시간 선택", "break_start")
        }
    }

    private fun setupDayButtons() {
        // 요일 버튼들을 리스트로 묶어서 한 번에 처리
        val dayButtons = listOf(
            binding.btnDayMon, binding.btnDayTue, binding.btnDayWed,
            binding.btnDayThu, binding.btnDayFri, binding.btnDaySat, binding.btnDaySun
        )

        dayButtons.forEach { button ->
            button.setOnClickListener {
                // 클릭할 때마다 선택 상태를 반전시킴 (색상 변경 셀렉터와 연동)
                it.isSelected = !it.isSelected
            }
        }
    }

    private fun showTimeRangePicker(onTimeSelected: (String, String) -> Unit) {
        val cal = java.util.Calendar.getInstance()

        // 1. 시작 시간 선택 (이미지처럼 스피너 모드로 강제하려면 테마 적용 필요)
        val startPicker = android.app.TimePickerDialog(
            requireContext(),
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar, // 스피너 스타일 테마
            { _, startHour, startMinute ->
                val startTime = String.format("%02d:%02d", startHour, startMinute)

                // 2. 시작 시간 선택 완료 후 바로 종료 시간 선택 팝업 띄움
                val endPicker = android.app.TimePickerDialog(
                    requireContext(),
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    { _, endHour, endMinute ->
                        val endTime = String.format("%02d:%02d", endHour, endMinute)
                        // 최종 결과 전달
                        onTimeSelected(startTime, endTime)
                    },
                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                    cal.get(java.util.Calendar.MINUTE),
                    true // 24시간 형식 사용
                )
                endPicker.setTitle("종료 시간 선택")
                endPicker.show()
            },
            cal.get(java.util.Calendar.HOUR_OF_DAY),
            cal.get(java.util.Calendar.MINUTE),
            true
        )
        startPicker.setTitle("시작 시간 선택")
        startPicker.show()
    }

    private fun setupResultListeners() {
        // 영업시간 처리
        parentFragmentManager.setFragmentResultListener("operating_start", viewLifecycleOwner) { _, bundle ->
            val start = bundle.getString("selectedTime")
            showPicker("영업 종료 시간 선택", "operating_end")
            parentFragmentManager.setFragmentResultListener("operating_end", viewLifecycleOwner) { _, endBundle ->
                val end = endBundle.getString("selectedTime")
                binding.tvOperatingTime.text = "$start  ~  $end"
                validateInputs()
            }
        }

        // 브레이크타임 처리
        parentFragmentManager.setFragmentResultListener("break_start", viewLifecycleOwner) { _, bundle ->
            val start = bundle.getString("selectedTime")
            showPicker("브레이크타임 종료 시간 선택", "break_end")
            parentFragmentManager.setFragmentResultListener("break_end", viewLifecycleOwner) { _, endBundle ->
                val end = endBundle.getString("selectedTime")
                binding.tvBreakTime.text = "$start  ~  $end"
                validateInputs()
            }
        }
    }

    private fun showPicker(title: String, requestKey: String) {
        TimePickerBottomSheet.newInstance(title, requestKey).show(parentFragmentManager, "TimePicker")
    }

    private fun validateInputs() {
        // A. 정기휴무일 영역 검사
        val hasHolidaySelected = binding.btnHasHoliday.isSelected
        val noHolidaySelected = binding.btnNoHoliday.isSelected
        val holidayTypeSelected = hasHolidaySelected || noHolidaySelected

        // 휴무일이 있다면 최소 한 개의 요일은 선택되어야 함
        val holidayDetailValid = if (hasHolidaySelected) {
            val dayButtons = listOf(
                binding.btnDayMon, binding.btnDayTue, binding.btnDayWed,
                binding.btnDayThu, binding.btnDayFri, binding.btnDaySat, binding.btnDaySun
            )
            dayButtons.any { it.isSelected }
        } else {
            true // 휴무일이 없으면 상세 요일은 체크 안 함
        }

        // B. 브레이크타임 영역 검사
        val hasBreakSelected = binding.btnHasBreak.isSelected
        val noBreakSelected = binding.btnNoBreak.isSelected
        val breakTypeSelected = hasBreakSelected || noBreakSelected

        // 브레이크타임이 있다면 시간 텍스트가 초기값과 달라야 함
        val breakDetailValid = if (hasBreakSelected) {
            binding.tvBreakTime.text.toString() != "15:00  ~  16:00"
        } else {
            true
        }

        // C. 영업시간 검사 (초기값과 다르면 입력한 것으로 간주)
        val operatingTimeValid = binding.tvOperatingTime.text.toString() != "08:00  ~  20:00"

        // 모든 조건 결합
        val isAllValid = holidayTypeSelected && holidayDetailValid &&
                breakTypeSelected && breakDetailValid && operatingTimeValid

        updateNextButtonState(isAllValid)
    }

    private fun updateNextButtonState(isEnabled: Boolean) {
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(isEnabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreHoursFragment()
    }
}