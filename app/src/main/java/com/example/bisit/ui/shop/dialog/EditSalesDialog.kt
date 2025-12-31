package com.example.bisit.ui.shop.dialog

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogEditSalesBinding

// 매출(계좌) 수정 다이얼로그
class EditSalesDialog(
    private val initialAccount: String,
    private val onResult: ((String) -> Unit)? = null   // 🔹 타입만 변경
) : DialogFragment() {

    private var _b: DialogEditSalesBinding? = null
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
        _b = DialogEditSalesBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 초기 값 세팅 (조회 상태) */
        b.etAccount.setText(initialAccount)

        /** 초기 버튼 상태 = 비활성 */
        updateSubmitButton(false)

        /** 닫기(X) 버튼 */
        b.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        /** 입력 변경 감지 */
        b.etAccount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        /** 수정하기 버튼 */
        b.btnSave.setOnClickListener {
            if (!b.btnSave.isEnabled) return@setOnClickListener

            val newAccount = b.etAccount.text.toString().trim()

            onResult?.invoke(newAccount)   // 🔹 Boolean → 실제 값 전달
            dismissAllowingStateLoss()

            InfoDialog("수정 완료되었습니다.")
                .show(parentFragmentManager, "info")
        }
    }

    /** 입력값 + 변경 여부 검사 */
    private fun validate() {
        val current = b.etAccount.text?.toString()?.trim().orEmpty()

        val isFilled = current.isNotEmpty()
        val isChanged = current != initialAccount

        updateSubmitButton(isFilled && isChanged)
    }

    /** 버튼 스타일 제어 */
    private fun updateSubmitButton(enabled: Boolean) {
        b.btnSave.isEnabled = enabled
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = requireActivity().windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
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
