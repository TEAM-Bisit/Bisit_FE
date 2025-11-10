package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogAddServiceBinding
import com.example.bisit.ui.shop.model.ServiceItem

// 서비스 추가/수정 다이얼로그
class AddServiceDialog(
    private val prefill: ServiceItem? = null,
    private val onSaved: (ServiceItem) -> Unit
) : DialogFragment() {

    private var _b: DialogAddServiceBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogAddServiceBinding.inflate(inflater, container, false)
        return b.root
    }

    // 시간/분 선택 BottomSheet 호출
    private fun showDurationPicker() {
        val currentH = b.etHour.text?.toString()?.replace("시간", "")?.toIntOrNull() ?: 0
        val currentM = b.etMin.text?.toString()?.replace("분", "")?.toIntOrNull() ?: 0
        TimePickerBottomSheet(currentH, currentM) { h, m ->
            b.etHour.text = "${h}시간"
            b.etMin.text = "${m}분"
        }.show(parentFragmentManager, "durPick")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 수정 모드일 경우 데이터 미리 채우기
        prefill?.let {
            b.etTitle.setText(it.title)
            b.etPrice.setText(it.price.toString())
            val h = it.durationMin / 60
            val m = it.durationMin % 60
            b.etHour.text = "${h}시간"
            b.etMin.text = "${m}분"
            b.etDesc.setText(it.desc)
        }

        // 시간/분 클릭 시 시간 선택
        b.etHour.setOnClickListener { showDurationPicker() }
        b.etMin.setOnClickListener { showDurationPicker() }

        // 추가 버튼 클릭 시 데이터 전달
        b.btnAdd.setOnClickListener {
            val title = b.etTitle.text?.toString().orEmpty()
            val price = b.etPrice.text?.toString()?.toIntOrNull() ?: 0
            val h = b.etHour.text?.toString()?.replace("시간", "")?.toIntOrNull() ?: 0
            val m = b.etMin.text?.toString()?.replace("분", "")?.toIntOrNull() ?: 0
            val desc = b.etDesc.text?.toString().orEmpty()

            val item = (prefill ?: ServiceItem(0, title, desc, price)).copy(
                title = title,
                desc = desc,
                price = price,
                durationMin = h * 60 + m
            )

            onSaved(item)
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
