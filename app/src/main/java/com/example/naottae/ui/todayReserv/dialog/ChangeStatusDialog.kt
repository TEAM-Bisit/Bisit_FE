package com.example.naottae.ui.todayReserv.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.naottae.R

class ChangeStatusDialog(
    private val currentStatus: String,
    private val onConfirm: (String) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.dialog_change_status, container, false)

        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupStatus)
        val confirmButton = view.findViewById<AppCompatButton>(R.id.btnConfirm)

        val current = when (currentStatus) {
            "확정 대기" -> R.id.radioWaiting
            "예약 확정" -> R.id.radioConfirmed
            "취소" -> R.id.radioCancelled
            "노쇼" -> R.id.radioNoShow
            "시술 완료" -> R.id.radioDone
            else -> R.id.radioWaiting
        }
        radioGroup.check(current)

        confirmButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedText = view.findViewById<RadioButton>(selectedId).text.toString()

            if (selectedText == "취소") {
                dismiss()
                ChangeCancelReasonDialog().show(parentFragmentManager, "cancel_reason")
            } else {
                onConfirm(selectedText)
                dismiss()
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val displayMetrics = resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.78f).toInt()

            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
