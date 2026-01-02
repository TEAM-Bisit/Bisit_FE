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
import androidx.lifecycle.lifecycleScope
import com.example.bisit.databinding.DialogEditShopIntroBinding
import com.example.bisit.data.model.shop.ShopPhotoItem
import com.example.bisit.ui.shop.adapter.ShopPhotoAdapter
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditShopIntroDialog(
    private val initialIntro: String,
    private val initialServiceType: String, // "VISIT" | "SHOP"

    /** Fragment의 photoViewModel.photos */
    private val photoFlow: StateFlow<List<ShopPhotoItem>>,

    /** 사진 추가 요청 (Fragment → ViewModel) */
    private val onAddPhotoClick: () -> Unit,

    /** 사진 삭제 요청 (Fragment → ViewModel) */
    private val onDeletePhotoClick: (photoId: Long) -> Unit,

    /** 저장 */
    private val onSaved: (
        intro: String,
        serviceType: String,
        photos: List<ShopPhotoItem>
    ) -> Unit
) : DialogFragment() {

    private var _b: DialogEditShopIntroBinding? = null
    private val b get() = _b!!

    private lateinit var photoAdapter: ShopPhotoAdapter
    private var currentPhotos: List<ShopPhotoItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        /* ===================== 닫기 ===================== */
        b.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        /* ===================== 초기값 ===================== */
        b.etStoreIntro.setText(initialIntro)

        when (initialServiceType) {
            "VISIT" -> b.rbVisitService.isChecked = true
            "SHOP" -> b.rbShopService.isChecked = true
        }

        /* ===================== 사진 어댑터 ===================== */
        photoAdapter = ShopPhotoAdapter(
            onAddClick = { onAddPhotoClick() },
            onDeleteClick = { photoId -> onDeletePhotoClick(photoId) }
        )

        b.rvStoreImages.adapter = photoAdapter

        /* ===================== 사진 상태 관찰 ===================== */
        viewLifecycleOwner.lifecycleScope.launch {
            photoFlow.collect { photos ->
                currentPhotos = photos
                photoAdapter.submitList(photos)
                validate()
            }
        }

        /* ===================== 입력 감지 ===================== */
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = validate()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }

        b.etStoreIntro.addTextChangedListener(watcher)
        b.rgServiceType.setOnCheckedChangeListener { _, _ -> validate() }

        updateSaveButton(false)

        /* ===================== 저장 ===================== */
        b.btnSave.setOnClickListener {
            if (!b.btnSave.isEnabled) return@setOnClickListener

            val intro = b.etStoreIntro.text.toString()
            val serviceType =
                if (b.rbVisitService.isChecked) "VISIT" else "SHOP"

            onSaved(
                intro,
                serviceType,
                currentPhotos
            )

            dismissAllowingStateLoss()
        }
    }

    /* ===================== 검증 ===================== */

    private fun validate() {
        val intro = b.etStoreIntro.text.toString().trim()
        val serviceType =
            if (b.rbVisitService.isChecked) "VISIT"
            else if (b.rbShopService.isChecked) "SHOP"
            else null

        val isFilled =
            intro.isNotBlank() &&
                    serviceType != null &&
                    currentPhotos.isNotEmpty()

        val isChanged =
            intro != initialIntro ||
                    serviceType != initialServiceType ||
                    currentPhotos.isNotEmpty() // 사진이 있으면 변경으로 간주

        updateSaveButton(isFilled && isChanged)
    }

    private fun updateSaveButton(enabled: Boolean) {
        b.btnSave.isEnabled = enabled
    }

    /* ===================== Dialog 크기 ===================== */

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
