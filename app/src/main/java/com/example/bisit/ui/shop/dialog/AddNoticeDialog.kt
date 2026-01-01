package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.example.bisit.R
import com.example.bisit.databinding.DialogAddNoticeBinding

class AddNoticeDialog(
    private val prefillTitle: String? = null,
    private val prefillContent: String? = null,
    private val onSaved: (title: String, content: String) -> Unit
) : DialogFragment() {

    private var _b: DialogAddNoticeBinding? = null
    private val b get() = _b!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // X 버튼으로만 닫히게
        isCancelable = false
    }

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

        /** X 버튼 */
        b.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        /** placeholder / 입력 글씨 색 */
        b.etTitle.setHintTextColor("#9AA1AF".toColorInt())
        b.etContent.setHintTextColor("#9AA1AF".toColorInt())
        b.etTitle.setTextColor("#222222".toColorInt())
        b.etContent.setTextColor("#222222".toColorInt())

        /** 수정 모드 */
        if (prefillTitle != null && prefillContent != null) {
            b.etTitle.setText(prefillTitle)
            b.etContent.setText(prefillContent)
            b.btnSubmit.text = getString(R.string.edit)
        }

        /** 초기 버튼 비활성 */
        updateSubmitButton(false)

        /** 입력 감지 */
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        b.etTitle.addTextChangedListener(watcher)
        b.etContent.addTextChangedListener(watcher)

        /** 저장 */
        b.btnSubmit.setOnClickListener {
            if (!b.btnSubmit.isEnabled) return@setOnClickListener

            val title = b.etTitle.text.toString().trim()
            val content = b.etContent.text.toString().trim()

            onSaved(title, content)
            dismissAllowingStateLoss()
        }
    }

    /** 입력 검증 */
    private fun validateForm() {
        val title = b.etTitle.text.toString().trim()
        val content = b.etContent.text.toString().trim()

        val isFilled = title.isNotEmpty() && content.isNotEmpty()
        val isChanged =
            if (prefillTitle != null && prefillContent != null) {
                title != prefillTitle || content != prefillContent
            } else {
                true
            }

        updateSubmitButton(isFilled && isChanged)
    }

    /** 버튼 활성화 */
    private fun updateSubmitButton(enabled: Boolean) {
        b.btnSubmit.isEnabled = enabled
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val wm = requireActivity().windowManager.currentWindowMetrics
                    val insets = wm.windowInsets.getInsets(WindowInsets.Type.systemBars())
                    wm.bounds.width() - insets.left - insets.right
                } else {
                    resources.displayMetrics.widthPixels
                }

            setLayout(
                (screenWidth * 0.806f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
