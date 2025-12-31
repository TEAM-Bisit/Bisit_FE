package com.example.bisit.ui.shop.dialog

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogEditShopIntroBinding
import com.example.bisit.ui.signUp.StorePhotoAdapter

class EditShopIntroDialog(
    private val initialIntro: String,
    private val initialServiceType: String, // "VISIT" | "SHOP"
    private val initialImages: List<Uri>,
    private val onSaved: (String, String, List<Uri>) -> Unit
) : DialogFragment() {

    private var _b: DialogEditShopIntroBinding? = null
    private val b get() = _b!!

    private val photoList = mutableListOf<Uri>()
    private lateinit var photoAdapter: StorePhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // X 버튼으로만 닫히도록 설정
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = DialogEditShopIntroBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 버튼 클릭 시에만 닫기
        b.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        // 초기값 세팅 (조회)
        b.etStoreIntro.setText(initialIntro)
        photoList.addAll(initialImages)

        when (initialServiceType) {
            "VISIT" -> b.rbVisitService.isChecked = true
            "SHOP" -> b.rbShopService.isChecked = true
        }

        photoAdapter = StorePhotoAdapter(
            onAddClick = { /* 이미지 추가 */ },
            onDeleteClick = { pos ->
                photoList.removeAt(pos)
                photoAdapter.submitList(photoList.toList())
                validate()
            }
        )

        b.rvStoreImages.adapter = photoAdapter
        photoAdapter.submitList(photoList.toList())

        updateSaveButton(false)

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { validate() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        b.etStoreIntro.addTextChangedListener(watcher)
        b.rgServiceType.setOnCheckedChangeListener { _, _ -> validate() }

        b.btnSave.setOnClickListener {
            if (!b.btnSave.isEnabled) return@setOnClickListener

            val intro = b.etStoreIntro.text.toString()
            val serviceType =
                if (b.rbVisitService.isChecked) "VISIT" else "SHOP"

            onSaved(intro, serviceType, photoList)
            dismissAllowingStateLoss()
        }
    }

    private fun validate() {
        val intro = b.etStoreIntro.text.toString()
        val serviceType =
            if (b.rbVisitService.isChecked) "VISIT"
            else if (b.rbShopService.isChecked) "SHOP"
            else null

        val isFilled = intro.isNotBlank() && serviceType != null && photoList.isNotEmpty()

        val isChanged =
            intro != initialIntro ||
                    serviceType != initialServiceType ||
                    photoList != initialImages

        updateSaveButton(isFilled && isChanged)
    }

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

            setLayout((screenWidth * 0.806f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
