package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogEditSalesBinding

// 매출(계좌) 수정 — 성공/실패 안내까지
class EditSalesDialog(
    private val onResult: ((Boolean) -> Unit)? = null
) : DialogFragment() {

    private var _b: DialogEditSalesBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogEditSalesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.btnSubmit.setOnClickListener {
            val ok = b.etAccount.text?.isNotBlank() == true
            val dialog = if (ok) {
                InfoDialog("수정 완료되었습니다.")
            } else {
                InfoDialog("인증에 실패하였습니다.")
            }
            dialog.show(parentFragmentManager, "info")
            onResult?.invoke(ok)
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            // 화면 너비의 80%로 다이얼로그 크기 설정
            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = requireActivity().windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
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
