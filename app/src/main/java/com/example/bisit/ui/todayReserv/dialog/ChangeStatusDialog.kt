package com.example.bisit.ui.todayReserv.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.bisit.R

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

        // 현재 상태에 따른 기본 선택 라디오 버튼 지정
        val currentCheckedId = when (currentStatus) {
            "확정 대기", "PENDING" -> R.id.radioWaiting
            "예약 확정", "CONFIRMED", "CUSTOMER_CONFIRMED" -> R.id.radioConfirmed
            "취소", "CANCELED_BY_CUSTOMER", "CANCELED_BY_SHOP" -> R.id.radioCancelled
            "노쇼", "NO_SHOW" -> R.id.radioNoShow
            "시술 완료", "COMPLETED" -> R.id.radioDone
            else -> R.id.radioWaiting
        }
        radioGroup.check(currentCheckedId)

        confirmButton.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedText = view.findViewById<RadioButton>(selectedId).text.toString()

            // 한글 UI 텍스트 → 서버 상태 코드로 변환
            val mappedStatus = when (selectedText) {
                "확정 대기" -> "PENDING"
                "예약 확정" -> "CONFIRMED"
                "취소" -> "CANCELED_BY_SHOP"
                "노쇼" -> "NO_SHOW"
                "시술 완료" -> "COMPLETED"
                else -> currentStatus
            }

            // 취소 사유 필요
            if (mappedStatus == "CANCELED_BY_SHOP") {
                dismiss()
                ChangeCancelReasonDialog().show(parentFragmentManager, "cancel_reason")
                return@setOnClickListener
            }

            // 즉시 상태 반영
            onConfirm(mappedStatus)
            dismiss()
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
