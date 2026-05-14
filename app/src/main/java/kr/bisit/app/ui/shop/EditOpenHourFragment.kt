package kr.bisit.app.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kr.bisit.app.data.model.shop.UpdateBusinessHourItem
import kr.bisit.app.databinding.FragmentEditOpenHourBinding
import kr.bisit.app.ui.shop.dialog.TimePickerBottomSheet
import kotlinx.coroutines.launch
import java.util.Locale

class EditOpenHourFragment : Fragment() {

    private var _binding: FragmentEditOpenHourBinding? = null
    private val b get() = _binding!!

    /* ===================== ViewModel ===================== */

    // shopId 공유
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    // 영업시간 전용
    private val businessHourViewModel: ShopBusinessHourViewModel by viewModels()

    /* ===================== 상태 ===================== */

    private var hasBreak = false

    private lateinit var dayButtons: Map<String, AppCompatButton>

    /* ===================== 생명주기 ===================== */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditOpenHourBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDayButtons()
        setupHolidayUi()
        setupBreakUi()
        setupTimePickers()

        observeShopId()
        observeBusinessHours()
        setupSave()

        b.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    /* ===================== shopId ===================== */

    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId ?: return@collect
                businessHourViewModel.setShopId(shopId)
            }
        }
    }

    /* ===================== 요일 버튼 ===================== */

    private fun setupDayButtons() {
        dayButtons = mapOf(
            "MONDAY" to b.btnDayMon,
            "TUESDAY" to b.btnDayTue,
            "WEDNESDAY" to b.btnDayWed,
            "THURSDAY" to b.btnDayThu,
            "FRIDAY" to b.btnDayFri,
            "SATURDAY" to b.btnDaySat,
            "SUNDAY" to b.btnDaySun
        )

        dayButtons.values.forEach { button ->
            button.setOnClickListener {
                button.isSelected = !button.isSelected
            }
        }
    }

    /* ===================== GET → UI ===================== */

    private fun observeBusinessHours() {
        viewLifecycleOwner.lifecycleScope.launch {
            businessHourViewModel.businessHours.collect { list ->
                if (list.isEmpty()) return@collect

                // 요일별 휴무 반영
                list.forEach { item ->
                    dayButtons[item.day]?.isSelected = item.isClosed
                }

                // 영업 중인 요일 하나 기준으로 시간 표시
                val openDay = list.firstOrNull { !it.isClosed }

                if (openDay != null) {
                    b.btnNoHoliday.isSelected = true
                    b.btnHasHoliday.isSelected = false
                    b.layoutHolidaySelection.visibility = View.GONE

                    b.tvOperatingTime.text =
                        "${openDay.openFrom}  ~  ${openDay.openTo}"

                    hasBreak = openDay.breakFrom != null
                    setBreakUi(hasBreak)

                    if (hasBreak) {
                        b.tvBreakTime.text =
                            "${openDay.breakFrom}  ~  ${openDay.breakTo}"
                    }
                } else {
                    // 전부 휴무
                    b.btnHasHoliday.isSelected = true
                    b.btnNoHoliday.isSelected = false
                    b.layoutHolidaySelection.visibility = View.VISIBLE
                }
            }
        }
    }

    /* ===================== 휴무 UI ===================== */

    private fun setupHolidayUi() {
        b.btnHasHoliday.setOnClickListener {
            b.btnHasHoliday.isSelected = true
            b.btnNoHoliday.isSelected = false
            b.layoutHolidaySelection.visibility = View.VISIBLE
        }

        b.btnNoHoliday.setOnClickListener {
            b.btnHasHoliday.isSelected = false
            b.btnNoHoliday.isSelected = true
            b.layoutHolidaySelection.visibility = View.GONE

            // 휴무 요일 전체 해제
            dayButtons.values.forEach { it.isSelected = false }
        }
    }

    /* ===================== 브레이크 UI ===================== */

    private fun setupBreakUi() {
        b.btnHasBreak.setOnClickListener {
            hasBreak = true
            setBreakUi(true)
        }

        b.btnNoBreak.setOnClickListener {
            hasBreak = false
            setBreakUi(false)
            b.tvBreakTime.text = "00:00  ~  00:00"
        }
    }

    private fun setBreakUi(has: Boolean) {
        b.btnHasBreak.isSelected = has
        b.btnNoBreak.isSelected = !has
        b.layoutBreakTimeSetting.visibility =
            if (has) View.VISIBLE else View.GONE
    }

    /* ===================== 시간 선택 ===================== */

    private fun setupTimePickers() {

        fun pick(init: Pair<Int, Int>, onPicked: (String) -> Unit) {
            TimePickerBottomSheet(init.first, init.second) { h, m ->
                onPicked(String.format(Locale.getDefault(), "%02d:%02d", h, m))
            }.show(parentFragmentManager, "time_picker")
        }

        b.tvOperatingTime.setOnClickListener {
            pick(8 to 0) { start ->
                pick(20 to 0) { end ->
                    b.tvOperatingTime.text = "$start  ~  $end"
                }
            }
        }

        b.tvBreakTime.setOnClickListener {
            pick(15 to 0) { start ->
                pick(16 to 0) { end ->
                    b.tvBreakTime.text = "$start  ~  $end"
                }
            }
        }
    }

    /* ===================== 저장 (PUT) ===================== */

    private fun setupSave() {

        fun parse(text: String): Pair<String, String>? {
            val parts = text.split("  ~  ")
            return if (parts.size == 2) parts[0] to parts[1] else null
        }

        b.btnSave.setOnClickListener {

            val op = parse(b.tvOperatingTime.text.toString())
            val br = parse(b.tvBreakTime.text.toString())

            val items = dayButtons.map { (day, button) ->
                val isClosed = button.isSelected

                UpdateBusinessHourItem(
                    day = day,
                    openFrom = if (isClosed) null else op?.first,
                    openTo = if (isClosed) null else op?.second,
                    breakFrom = if (!isClosed && hasBreak) br?.first else null,
                    breakTo = if (!isClosed && hasBreak) br?.second else null,
                    validOpenHours = true,
                    validBreakTimeComplete = true,
                    validBreakHours = true,
                    breakTimeWithinOpenHours = true
                )
            }

            businessHourViewModel.updateBusinessHours(items) {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
