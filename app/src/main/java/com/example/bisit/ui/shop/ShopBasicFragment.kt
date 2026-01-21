package com.example.bisit.ui.shop

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.bisit.R
import com.example.bisit.databinding.FragmentShopBasicBinding
import com.example.bisit.ui.shop.dialog.EditSalesDialog
import com.example.bisit.ui.shop.dialog.EditShopInfoDialog
import com.example.bisit.ui.shop.dialog.EditShopIntroDialog
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ShopBasicFragment : Fragment() {

    private var _binding: FragmentShopBasicBinding? = null
    private val binding get() = _binding!!

    /* ===================== ViewModel ===================== */

    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    private val viewModel: ShopBasicViewModel by viewModels()
    private val photoViewModel: ShopPhotoViewModel by viewModels()

    /* ===================== 화면 상태 ===================== */

    private var currentIntro: String = ""
    private var currentServiceType: String = "VISIT"
    private var isOpenHourExpanded = false

    /* ===================== 이미지 선택 ===================== */

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            uploadUriAsMultipart(uri)
        }

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

        observeShopId()
        observeViewModel()
        observePhotos()
        setupClickListeners()
    }

    /* ===================== shopId Observe ===================== */

    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId ?: return@collect

                viewModel.setShopId(shopId)
                photoViewModel.setShopId(shopId)

                viewModel.fetchShopDetail()
                viewModel.fetchShopIntro()
                viewModel.fetchShopAccount()

                photoViewModel.fetchPhotos()
            }
        }
    }

    /* ===================== ViewModel Observe ===================== */

    private fun observeViewModel() {

        // 매장 상세
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopDetail.collect { detail ->
                detail ?: return@collect
                binding.tvShopName.text = detail.shopName
                binding.tvShopPhone.text = detail.phone
                binding.tvShopAddress.text =
                    "${detail.address} ${detail.detailAddress}"
            }
        }

        // 매장 소개
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopIntro.collect { intro ->
                intro ?: return@collect

                currentIntro = intro.intro
                currentServiceType = intro.serviceChannel

                binding.tvShopIntro.text = intro.intro
                binding.tvShopService.text =
                    if (intro.serviceChannel == "VISIT") "방문 서비스" else "매장 서비스"
            }
        }

        // 정산 계좌
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopAccount.collect { account ->
                account ?: return@collect
                binding.tvSalesAccount.text =
                    "${account.bankName} ${account.accountNumber} ${account.accountHolder}"
            }
        }

        // 영업 시간
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopOpenHour.collect { openHour ->
                openHour ?: return@collect

                binding.tvOpenInfo.text =
                    if (openHour.isOpen) {
                        "영업중 ${openHour.openTime} ~ ${openHour.closeTime}"
                    } else {
                        "영업 종료"
                    }

                binding.layoutOpenHourDetail.removeAllViews()

                openHour.weeklyHours.forEach { text ->
                    val tv = TextView(requireContext()).apply {
                        this.text = text
                        textSize = 13f
                        setTextColor(0xFF222222.toInt())
                    }
                    binding.layoutOpenHourDetail.addView(tv)
                }
            }
        }
    }

    /* ===================== 대표 이미지 Observe ===================== */

    private fun observePhotos() {
        viewLifecycleOwner.lifecycleScope.launch {
            photoViewModel.photos.collect { photos ->
                val mainPhoto = photos.firstOrNull()

                if (mainPhoto != null) {
                    Glide.with(binding.imgHeader)
                        .load(mainPhoto.url)
                        .centerCrop()
                        .into(binding.imgHeader)
                } else {
                    binding.imgHeader.setImageResource(R.drawable.sample_header)
                }
            }
        }
    }

    /* ===================== 클릭 ===================== */

    private fun setupClickListeners() {

        // 매장 기본 정보 수정
        binding.btnEditShopInfo.setOnClickListener {
            EditShopInfoDialog(
                initialName = binding.tvShopName.text.toString(),
                initialPhone = binding.tvShopPhone.text.toString(),
                initialAddress = binding.tvShopAddress.text.toString(),
                onSaved = { name, phone, addressLine, detailAddress ->
                    viewModel.updateShopBasicInfo(
                        name = name,
                        phone = phone,
                        addressLine = addressLine,
                        detailAddress = detailAddress
                    )
                }
            ).show(parentFragmentManager, "edit_shop_info")
        }

        // 매장 소개 / 대표 이미지 변경
        binding.btnEditIntro.setOnClickListener { openIntroDialog() }
        binding.btnChangeHeader.setOnClickListener { openIntroDialog() }

        // 정산 계좌 수정
        binding.btnEditSales.setOnClickListener {
            EditSalesDialog(
                initialAccount = binding.tvSalesAccount.text.toString(),
                onResult = { newAccount ->
                    viewModel.updateShopAccount(
                        bankCode = "004",
                        accountNumber = newAccount,
                        accountHolder = "정원렬"
                    )
                }
            ).show(parentFragmentManager, "edit_sales")
        }

        // 영업시간 펼치기
        binding.btnExpandHour.setOnClickListener {
            isOpenHourExpanded = !isOpenHourExpanded

            binding.layoutOpenHourDetail.visibility =
                if (isOpenHourExpanded) View.VISIBLE else View.GONE

            binding.btnExpandHour.animate()
                .rotation(if (isOpenHourExpanded) 180f else 0f)
                .setDuration(200)
                .start()
        }

        binding.btnEditOpenHour.setOnClickListener {
            findNavController().navigate(
                R.id.action_shopBasicFragment_to_editOpenHourFragment
            )
        }
    }

    /* ===================== Intro Dialog ===================== */

    private fun openIntroDialog() {
        EditShopIntroDialog(
            initialIntro = currentIntro,
            initialServiceType = currentServiceType,
            photoFlow = photoViewModel.photos,

            onAddPhotoClick = {
                pickImageLauncher.launch("image/*")
            },

            onDeletePhotoClick = { photoId ->
                photoViewModel.deletePhoto(photoId)
            },

            onSaved = { intro, serviceType, photos ->
                viewModel.updateShopIntro(
                    intro = intro,
                    serviceChannel = serviceType,
                    photoIds = photos.map { it.id }
                )
            }
        ).show(parentFragmentManager, "edit_intro")
    }

    /* ===================== Uri → Multipart ===================== */

    private fun uploadUriAsMultipart(uri: Uri) {
        val resolver = requireContext().contentResolver
        val inputStream = resolver.openInputStream(uri) ?: return
        val bytes = inputStream.readBytes()

        val requestBody =
            bytes.toRequestBody("image/*".toMediaTypeOrNull())

        val part = MultipartBody.Part.createFormData(
            name = "file",
            filename = "shop_photo.jpg",
            body = requestBody
        )

        photoViewModel.uploadPhoto(part)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
