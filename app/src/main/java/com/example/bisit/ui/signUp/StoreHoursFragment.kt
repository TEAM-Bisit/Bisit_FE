package com.example.bisit.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.ShopApiService
import com.example.bisit.data.model.shop.*
import com.example.bisit.databinding.FragmentStoreHoursBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoreHoursFragment : Fragment() {

    private var _binding: FragmentStoreHoursBinding? = null
    private val binding get() = _binding!!

    // API 성공 시 shopId를 가져오기 위한 ViewModel
    private val signUpViewModel: SignUpViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreHoursBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 설정: 다음 단계 버튼 비활성화
        updateNextButtonState(false)

        setupListeners()
        setupResultListeners()
    }

    fun saveAllHoursData(onSuccess: () -> Unit) {
        val shopId = signUpViewModel.shopId.value ?: 2L // 테스트용 기본값 2
        val api = RetrofitClient.getStoreApi(requireContext())

        val holidayReq = ShopHolidayRequest(
            hasHoliday = binding.btnHasHoliday.isSelected,
            hasBreaktime = binding.btnHasBreak.isSelected
        )

        api.updateHolidaySettings(shopId, holidayReq).enqueue(object : Callback<ShopHolidayResponse> {
            override fun onResponse(call: Call<ShopHolidayResponse>, response: Response<ShopHolidayResponse>) {
                if (response.isSuccessful) {
                    // 성공 시 2단계(휴무 요일 설정)로 진행
                    saveHolidayDays(shopId, api, onSuccess)
                } else {
                    showToast("휴무 설정 저장에 실패했습니다.")
                }
            }
            override fun onFailure(call: Call<ShopHolidayResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. (Step 1)")
            }
        })
    }

    private fun saveHolidayDays(shopId: Long, api: ShopApiService, onSuccess: () -> Unit) {
        val selectedDays = mutableListOf<String>()
        val dayMap = mapOf(
            binding.btnDayMon to "MONDAY",
            binding.btnDayTue to "TUESDAY",
            binding.btnDayWed to "WEDNESDAY",
            binding.btnDayThu to "THURSDAY",
            binding.btnDayFri to "FRIDAY",
            binding.btnDaySat to "SATURDAY",
            binding.btnDaySun to "SUNDAY"
        )

        if (binding.btnHasHoliday.isSelected) {
            dayMap.forEach { (button, dayCode) ->
                if (button.isSelected) selectedDays.add(dayCode)
            }
        }

        val setHolidayReq = ShopSetHolidayRequest(day = selectedDays)

        api.setHolidayDays(shopId, setHolidayReq).enqueue(object : Callback<ShopOperatingHoursResponse> {
            override fun onResponse(call: Call<ShopOperatingHoursResponse>, response: Response<ShopOperatingHoursResponse>) {
                if (response.isSuccessful) {
                    saveOperatingHours(shopId, api, onSuccess)
                } else {
                    showToast("휴무 요일 저장에 실패했습니다.")
                }
            }
            override fun onFailure(call: Call<ShopOperatingHoursResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. (Step 2)")
            }
        })
    }

    private fun saveOperatingHours(shopId: Long, api: ShopApiService, onSuccess: () -> Unit) {
        val opTimeParts = binding.tvOperatingTime.text.toString().split("  ~  ")
        val brTimeParts = binding.tvBreakTime.text.toString().split("  ~  ")

        val hasBreak = binding.btnHasBreak.isSelected

        val hoursReq = ShopOperatingHoursRequest(
            openFrom = opTimeParts[0].trim(),
            openTo = opTimeParts[1].trim(),
            breakFrom = if (hasBreak) brTimeParts[0].trim() else null,
            breakTo = if (hasBreak) brTimeParts[1].trim() else null
        )

        api.updateOperatingHours(shopId, hoursReq).enqueue(object : Callback<ShopOperatingHoursResponse> {
            override fun onResponse(call: Call<ShopOperatingHoursResponse>, response: Response<ShopOperatingHoursResponse>) {
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    showToast("영업 시간 저장에 실패했습니다.")
                }
            }
            override fun onFailure(call: Call<ShopOperatingHoursResponse>, t: Throwable) {
                showToast("네트워크 오류가 발생했습니다. (Step 3)")
            }
        })
    }

    private fun setupListeners() {
        binding.btnHasHoliday.setOnClickListener {
            it.isSelected = true
            binding.btnNoHoliday.isSelected = false
            binding.layoutHolidaySelection.visibility = View.VISIBLE
            validateInputs()
        }

        binding.btnNoHoliday.setOnClickListener {
            it.isSelected = true
            binding.btnHasHoliday.isSelected = false
            binding.layoutHolidaySelection.visibility = View.GONE
            validateInputs()
        }

        setupDayButtons()

        binding.btnHasBreak.setOnClickListener {
            it.isSelected = true
            binding.btnNoBreak.isSelected = false
            binding.layoutBreakTimeSetting.visibility = View.VISIBLE
            validateInputs()
        }

        binding.btnNoBreak.setOnClickListener {
            it.isSelected = true
            binding.btnHasBreak.isSelected = false
            binding.layoutBreakTimeSetting.visibility = View.GONE
            validateInputs()
        }

        binding.tvOperatingTime.setOnClickListener {
            showPicker("영업 시작 시간 선택", "operating_start")
        }

        binding.tvBreakTime.setOnClickListener {
            showPicker("브레이크타임 시작 시간 선택", "break_start")
        }

        binding.tvHolidayCycle.setOnClickListener { view ->
            val popup = androidx.appcompat.widget.PopupMenu(requireContext(), view)
            popup.menu.add("매주")
            popup.menu.add("격주")
            popup.setOnMenuItemClickListener { item ->
                binding.tvHolidayCycleText.text = item.title
                validateInputs()
                true
            }
            popup.show()
        }
    }

    private fun setupDayButtons() {
        val dayButtons = listOf(
            binding.btnDayMon, binding.btnDayTue, binding.btnDayWed,
            binding.btnDayThu, binding.btnDayFri, binding.btnDaySat, binding.btnDaySun
        )
        dayButtons.forEach { button ->
            button.setOnClickListener {
                it.isSelected = !it.isSelected
                validateInputs()
            }
        }
    }

    private fun setupResultListeners() {
        parentFragmentManager.setFragmentResultListener("operating_start", viewLifecycleOwner) { _, bundle ->
            val start = bundle.getString("selectedTime") ?: "00:00"
            showPicker("영업 종료 시간 선택", "operating_end")

            parentFragmentManager.setFragmentResultListener("operating_end", viewLifecycleOwner) { _, endBundle ->
                val end = endBundle.getString("selectedTime") ?: "00:00"

                updateTimeDisplay(binding.tvOperatingTime, start, end)

                if (!isTimeValid(start, end)) {
                    Toast.makeText(requireContext(), "종료 시간이 시작 시간보다 빠릅니다.", Toast.LENGTH_SHORT).show()
                }
                validateInputs()
            }
        }

        parentFragmentManager.setFragmentResultListener("break_start", viewLifecycleOwner) { _, bundle ->
            val start = bundle.getString("selectedTime") ?: "00:00"
            showPicker("브레이크타임 종료 시간 선택", "break_end")

            parentFragmentManager.setFragmentResultListener("break_end", viewLifecycleOwner) { _, endBundle ->
                val end = endBundle.getString("selectedTime") ?: "00:00"

                updateTimeDisplay(binding.tvBreakTime, start, end)

                if (!isTimeValid(start, end)) {
                    Toast.makeText(requireContext(), "브레이크타임 설정이 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                }
                validateInputs()
            }
        }
    }

    private fun showPicker(title: String, requestKey: String) {
        TimePickerBottomSheet.newInstance(title, requestKey).show(parentFragmentManager, "TimePicker")
    }

    private fun validateInputs() {
        val hasHolidaySelected = binding.btnHasHoliday.isSelected
        val noHolidaySelected = binding.btnNoHoliday.isSelected
        val holidayTypeSelected = hasHolidaySelected || noHolidaySelected

        val holidayDetailValid = if (hasHolidaySelected) {
            val dayButtons = listOf(
                binding.btnDayMon, binding.btnDayTue, binding.btnDayWed,
                binding.btnDayThu, binding.btnDayFri, binding.btnDaySat, binding.btnDaySun
            )
            dayButtons.any { it.isSelected }
        } else true

        val hasBreakSelected = binding.btnHasBreak.isSelected
        val noBreakSelected = binding.btnNoBreak.isSelected
        val breakTypeSelected = hasBreakSelected || noBreakSelected

        val breakDetailValid = if (hasBreakSelected) {
            binding.tvBreakTime.text.toString() != "00:00  ~  00:00"
        } else true

        val operatingTimeValid = binding.tvOperatingTime.text.toString() != "00:00  ~  00:00"

        val opValid = try {
            val parts = binding.tvOperatingTime.text.toString().split("  ~  ")
            if (parts.size == 2) isTimeValid(parts[0], parts[1]) else false
        } catch (e: Exception) { false }

        val isAllValid = holidayTypeSelected && holidayDetailValid &&
                breakTypeSelected && breakDetailValid && operatingTimeValid && opValid

        updateNextButtonState(isAllValid)
    }

    private fun updateNextButtonState(isEnabled: Boolean) {
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(isEnabled)
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    private fun isTimeValid(start: String, end: String): Boolean {
        return try {
            val startParts = start.split(":")
            val endParts = end.split(":")

            val startMinutes = startParts[0].toInt() * 60 + startParts[1].toInt()
            val endMinutes = endParts[0].toInt() * 60 + endParts[1].toInt()

            endMinutes > startMinutes
        } catch (e: Exception) {
            false
        }
    }

    private fun updateTimeDisplay(textView: android.widget.TextView, start: String, end: String) {
        val isValid = isTimeValid(start, end)
        textView.text = "$start  ~  $end"

        if (isValid) {
            textView.setTextColor(android.graphics.Color.BLACK)
        } else {
            textView.setTextColor(android.graphics.Color.RED)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreHoursFragment()
    }
}