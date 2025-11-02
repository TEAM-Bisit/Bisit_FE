package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogConfirmBinding

class ConfirmDialog(
    private val message: String,
    private val okText: String = "확인",
    private val cancelText: String = "닫기",
    private val onOk: (() -> Unit)? = null
) : DialogFragment() {

    private var _b: DialogConfirmBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogConfirmBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.tvMessage.text = message
        b.btnOk.text = okText
        b.btnCancel.text = cancelText

        b.btnCancel.setOnClickListener { dismissAllowingStateLoss() }
        b.btnOk.setOnClickListener {
            onOk?.invoke()
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 배경 투명하게
            setBackgroundDrawableResource(android.R.color.transparent)

            // 화면 너비의 80.6%로 설정
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
