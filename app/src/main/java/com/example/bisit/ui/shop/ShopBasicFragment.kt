package com.example.bisit.ui.shop

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentShopBasicBinding
import com.example.bisit.ui.shop.dialog.EditSalesDialog
import com.example.bisit.ui.shop.dialog.EditShopInfoDialog
import com.example.bisit.ui.shop.dialog.EditShopIntroDialog

class ShopBasicFragment : Fragment() {

    private var _binding: FragmentShopBasicBinding? = null
    private val binding get() = _binding!!

    /** ===== 상태 변수 (중요) ===== */
    private var currentIntro: String = ""
    private var currentServiceType: String = "VISIT" // VISIT | SHOP
    private var currentIntroImages: List<Uri> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBasicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchShopInfo()
        fetchShopIntro()
        fetchSalesInfo()
        setupClickListeners()
    }

    /* ===================== GET ===================== */

    private fun fetchShopInfo() {
        val name = "장미헤어"
        val phone = "010-0000-0000"
        val address = "대구 중구 관덕정길 6-11 1층"

        binding.tvShopName.text = name
        binding.tvShopPhone.text = phone
        binding.tvShopAddress.text = address
    }

    private fun fetchShopIntro() {
        // 서버 GET 결과라고 가정
        currentIntro = "10년차 아티스트가 운영하는 장미헤어입니다."
        currentServiceType = "VISIT"

        currentIntroImages = listOf(
            "https://example.com/shop1.jpg".toUri(),
            "https://example.com/shop2.jpg".toUri()
        )

        binding.tvShopIntro.text = currentIntro
        binding.tvShopService.text =
            if (currentServiceType == "VISIT") "방문 서비스" else "매장 서비스"

        // 대표 이미지 = 첫 번째 이미지
        currentIntroImages.firstOrNull()?.let { uri ->
            // Glide / Coil 사용
            // Glide.with(this).load(uri).into(binding.imgHeader)
        }
    }

    private fun fetchSalesInfo() {
        binding.tvSalesAccount.text = "국민 6812********** 정원렬"
    }

    /* ===================== 클릭 ===================== */

    private fun setupClickListeners() {

        binding.btnEditShopInfo.setOnClickListener {
            EditShopInfoDialog(
                initialName = binding.tvShopName.text.toString(),
                initialPhone = binding.tvShopPhone.text.toString(),
                initialAddress = binding.tvShopAddress.text.toString(),
                onSaved = { fetchShopInfo() }
            ).show(parentFragmentManager, "edit_shop_info")
        }

        // 매장 소개 수정
        binding.btnEditIntro.setOnClickListener {
            openIntroDialog()
        }

        // 대표 이미지 변경 → 같은 모달
        binding.btnChangeHeader.setOnClickListener {
            openIntroDialog()
        }

        binding.btnEditSales.setOnClickListener {
            EditSalesDialog(
                initialAccount = binding.tvSalesAccount.text.toString(),
                onResult = { fetchSalesInfo() }
            ).show(parentFragmentManager, "edit_sales")
        }
    }

    private fun openIntroDialog() {
        EditShopIntroDialog(
            initialIntro = currentIntro,
            initialServiceType = currentServiceType,
            initialImages = currentIntroImages,
            onSaved = { intro, serviceType, images ->

                // 상태 갱신
                currentIntro = intro
                currentServiceType = serviceType
                currentIntroImages = images

                // 화면 반영
                fetchShopIntro()
            }
        ).show(parentFragmentManager, "edit_intro")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

