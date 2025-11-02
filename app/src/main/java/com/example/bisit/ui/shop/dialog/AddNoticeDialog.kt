package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogAddNoticeBinding
import com.example.bisit.ui.shop.model.Notice

class AddNoticeDialog(
    private val prefill: Notice? = null,
    private val onSaved: (Notice) -> Unit
) : DialogFragment() {

    private var _b: DialogAddNoticeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogAddNoticeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 기존 데이터 미리 채우기
        prefill?.let {
            b.etTitle.setText(it.title)
            b.etContent.setText(it.content)
        }

        // 저장 버튼 클릭
        b.btnSubmit.setOnClickListener {
            val title = b.etTitle.text?.toString().orEmpty()
            val content = b.etContent.text?.toString().orEmpty()
            val item = (prefill ?: Notice(0, title, content, date = "")).copy(
                title = title,
                content = content
            )
            onSaved(item)
            dismissAllowingStateLoss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            // 배경 투명 + 가운데 배치
            setBackgroundDrawableResource(android.R.color.transparent)

            // 화면 너비의 80.6%만큼만 사용
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
