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
import com.example.bisit.databinding.DialogEditShopInfoBinding

class EditShopInfoDialog(
    private val onSaved: (() -> Unit)? = null
) : DialogFragment() {

    private var _b: DialogEditShopInfoBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogEditShopInfoBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b.btnSearchAddr.setOnClickListener { }

        // EditText 변화 감지 → 버튼 활성화 상태 변경
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = b.etName.text?.isNotBlank() == true
                val phone = b.etPhone.text?.isNotBlank() == true
                val addr = b.etAddr.text?.isNotBlank() == true
                b.btnSave.isEnabled = name && phone && addr
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        b.etName.addTextChangedListener(watcher)
        b.etPhone.addTextChangedListener(watcher)
        b.etAddr.addTextChangedListener(watcher)

        b.btnSave.setOnClickListener {
//            val name = b.etName.text?.toString()?.trim().orEmpty()
//            val phone = b.etPhone.text?.toString()?.trim().orEmpty()
//            val addr = b.etAddr.text?.toString()?.trim().orEmpty()

            if (b.btnSave.isEnabled) {
                onSaved?.invoke()
                InfoDialog("매장 정보가 수정되었습니다.")
                    .show(parentFragmentManager, "info")
                dismissAllowingStateLoss()
            } else {
                InfoDialog("모든 항목을 입력해주세요.")
                    .show(parentFragmentManager, "info")
            }
        }
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
