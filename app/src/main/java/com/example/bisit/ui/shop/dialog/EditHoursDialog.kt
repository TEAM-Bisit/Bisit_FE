package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogEditHoursBinding
import java.util.Locale

// 영업시간 수정 — 칩 토글/시간 선택
class EditHoursDialog(
    private val onSaved: (() -> Unit)? = null
) : DialogFragment() {

    private var _b: DialogEditHoursBinding? = null
    private val b get() = _b!!

    private fun toggleSelected(v: View, selected: Boolean? = null) {
        v.isSelected = selected ?: !v.isSelected
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogEditHoursBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 휴무일 여부 토글
        fun setHoliday(has: Boolean) {
            b.chipHolidayYes.isSelected = has
            b.chipHolidayNo.isSelected = !has
            b.rowHolidayDay.visibility = if (has) View.VISIBLE else View.GONE
        }
        b.chipHolidayYes.setOnClickListener { setHoliday(true) }
        b.chipHolidayNo.setOnClickListener { setHoliday(false) }
        setHoliday(false)

        // 브레이크타임 여부 토글
        fun setBreak(has: Boolean) {
            b.chipBreakYes.isSelected = has
            b.chipBreakNo.isSelected = !has
            b.rowBreakTime.visibility = if (has) View.VISIBLE else View.GONE
        }
        b.chipBreakYes.setOnClickListener { setBreak(true) }
        b.chipBreakNo.setOnClickListener { setBreak(false) }
        setBreak(false)

        // 시간 선택 (영업/브레이크)
        fun pick(target: (Int, Int) -> Unit, init: Pair<Int, Int>) {
            TimePickerBottomSheet(init.first, init.second) { h, m ->
                target(h, m)
            }.show(parentFragmentManager, "time_pick")
        }

        b.etOpen.setOnClickListener {
            pick({ h, m ->
                b.etOpen.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }, Pair(8, 0))
        }

        b.etClose.setOnClickListener {
            pick({ h, m ->
                b.etClose.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }, Pair(20, 0))
        }

        b.etBreakStart.setOnClickListener {
            pick({ h, m ->
                b.etBreakStart.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }, Pair(15, 0))
        }

        b.etBreakEnd.setOnClickListener {
            pick({ h, m ->
                b.etBreakEnd.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
            }, Pair(16, 0))
        }

        // 저장 버튼 클릭
        b.btnSave.setOnClickListener {
            onSaved?.invoke()
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val wm = requireActivity().windowManager.currentWindowMetrics
                val insets = wm.windowInsets.getInsets(WindowInsets.Type.systemBars())
                wm.bounds.width() - insets.left - insets.right
            } else {
                val displayMetrics = resources.displayMetrics
                displayMetrics.widthPixels
            }

            val width = (screenWidth * 0.806f).toInt()
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            setLayout(width, height)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
