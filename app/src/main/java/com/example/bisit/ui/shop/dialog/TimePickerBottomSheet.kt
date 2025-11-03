package com.example.bisit.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.example.bisit.databinding.SheetTimePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.Locale

class TimePickerBottomSheet(
    private val initialHour: Int = 8,
    private val initialMinute: Int = 0,
    private val onPicked: (hour: Int, minute: Int) -> Unit
) : BottomSheetDialogFragment() {

    private var _b: SheetTimePickerBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = SheetTimePickerBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fun NumberPicker.setup(min: Int, max: Int, value: Int, formatter: (Int) -> String) {
            minValue = min
            maxValue = max
            this.value = value
            setFormatter { v -> formatter(v) }
            wrapSelectorWheel = true
        }

        b.pickerHour.setup(0, 23, initialHour) { String.format(Locale.getDefault(), "%02d", it) }
        b.pickerMinute.setup(0, 59, initialMinute) { String.format(Locale.getDefault(), "%02d", it) }

        b.btnApply.setOnClickListener {
            onPicked(b.pickerHour.value, b.pickerMinute.value)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
