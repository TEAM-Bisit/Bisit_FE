package com.example.bisit.ui.staffManage.modal

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogStaffRequestResultBinding

class StaffRequestResultDialog(
    private val message: String
) : DialogFragment() {

    private var _b: DialogStaffRequestResultBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogStaffRequestResultBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.tvMessage.text = message
        b.btnClose.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 배경 dim
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.8f)

            // width = 화면의 80.6%
            val screenWidth =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val wm = requireActivity().windowManager.currentWindowMetrics
                    val insets = wm.windowInsets.getInsets(WindowInsets.Type.systemBars())
                    wm.bounds.width() - insets.left - insets.right
                } else {
                    resources.displayMetrics.widthPixels
                }

            setLayout((screenWidth * 0.806f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
