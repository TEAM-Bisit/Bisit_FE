package com.example.bisit.ui.shop.dialog

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogEditShopInfoBinding
import com.example.bisit.ui.customerPay.AddressSearchActivity

class EditShopInfoDialog(
    private val initialName: String,
    private val initialPhone: String,
    private val initialAddress: String,
    private val onSaved: (() -> Unit)? = null
) : DialogFragment() {

    private var _b: DialogEditShopInfoBinding? = null
    private val b get() = _b!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // X 버튼으로만 닫히도록
        isCancelable = false
    }

    // 주소 검색 결과 수신
    private val addressLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val address =
                    result.data?.getStringExtra("selectedAddress") ?: ""
                b.etAddr.setText(address)
            }
        }

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

        /** X 버튼 클릭 시에만 닫기 */
        b.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        /** 기존 값 세팅 (조회 상태) */
        b.etName.setText(initialName)
        b.etPhone.setText(initialPhone)
        b.etAddr.setText(initialAddress)

        /** 초기 버튼 상태 = 비활성 */
        updateSaveButton(false)

        /** 주소 검색 */
        b.btnSearchAddr.setOnClickListener {
            val intent = Intent(requireContext(), AddressSearchActivity::class.java)
            addressLauncher.launch(intent)
        }

        /** 입력 변경 감지 */
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        b.etName.addTextChangedListener(watcher)
        b.etPhone.addTextChangedListener(watcher)
        b.etAddr.addTextChangedListener(watcher)

        /** 저장 */
        b.btnSave.setOnClickListener {
            if (!b.btnSave.isEnabled) return@setOnClickListener

            onSaved?.invoke()
            InfoDialog("매장 정보가 수정되었습니다.")
                .show(parentFragmentManager, "info")
            dismissAllowingStateLoss()
        }
    }

    /** 유효성 + 변경 여부 검사 */
    private fun validateForm() {
        val name = b.etName.text?.toString()?.trim().orEmpty()
        val phone = b.etPhone.text?.toString()?.trim().orEmpty()
        val addr = b.etAddr.text?.toString()?.trim().orEmpty()

        val allFilled = name.isNotEmpty() && phone.isNotEmpty() && addr.isNotEmpty()

        val isChanged =
            name != initialName ||
                    phone != initialPhone ||
                    addr != initialAddress

        updateSaveButton(allFilled && isChanged)
    }

    /** 버튼 스타일 제어 */
    private fun updateSaveButton(enabled: Boolean) {
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

            val width = (screenWidth * 0.806f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
