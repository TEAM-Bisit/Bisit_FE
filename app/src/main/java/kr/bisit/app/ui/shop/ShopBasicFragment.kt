package kr.bisit.app.ui.shop

import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.data.model.shop.ShopAccountResponse
import kr.bisit.app.databinding.FragmentShopBasicBinding
import kr.bisit.app.ui.shop.dialog.EditSalesDialog
import kr.bisit.app.ui.shop.dialog.EditShopInfoDialog
import kr.bisit.app.ui.shop.dialog.EditShopIntroDialog
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ShopBasicFragment : Fragment() {

    private var _binding: FragmentShopBasicBinding? = null
    private val binding get() = _binding!!

    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    private val viewModel: ShopBasicViewModel by viewModels()
    private val photoViewModel: ShopPhotoViewModel by viewModels()

    private var currentIntro: String = ""
    private var currentServiceType: String = "BISIT"
    private var currentAccount: ShopAccountResponse? = null
    private var isOpenHourExpanded = false

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

    override fun onResume() {
        super.onResume()
        refreshOnboarding()
    }

    fun refreshOnboarding() {

        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide()
            return
        }

        binding.root.post {

            if (activity.currentGuideStep ==
                MainActivity.GuideStep.EDIT_BUTTON
            ) {

                val rect1 = Rect()
                val rect2 = Rect()
                val rect3 = Rect()

                binding.btnEditShopInfo.getGlobalVisibleRect(rect1)
                binding.btnEditIntro.getGlobalVisibleRect(rect2)
                binding.btnEditSales.getGlobalVisibleRect(rect3)

                val rects = listOf(
                    RectF(rect1),
                    RectF(rect2),
                    RectF(rect3)
                )

                activity.showGlobalOverlayMultiple(
                    rects = rects,
                    shape = HighlightOverlayView.HighlightShape.CIRCLE,
                    radiusDp = 40f
                )

                showGuideTextLeftOfFirstButton(binding.btnEditShopInfo)
            }
        }
    }

    private fun getGuideLayer(): ViewGroup {
        return (requireActivity() as MainActivity).getGlobalGuideLayer()
    }

    private fun clearGuide() {
        val layer = getGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
    }

    private fun showGuideTextLeftOfFirstButton(targetView: View) {

        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE
        guideLayer.bringToFront()

        val guideText = TextView(requireContext()).apply {
            text = "입력하신 매장 정보는\n이곳에서 수정할 수 있어요"
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)

            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
            gravity = Gravity.END
        }

        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )

        params.gravity = Gravity.END or Gravity.TOP

        params.marginEnd = (100 * resources.displayMetrics.density).toInt()

        val rect = Rect()
        targetView.getGlobalVisibleRect(rect)

        val layerLocation = IntArray(2)
        guideLayer.getLocationOnScreen(layerLocation)

        val localTop = rect.top - layerLocation[1]
        params.topMargin = localTop

        guideText.layoutParams = params

        guideLayer.addView(guideText)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeShopId()
        observeViewModel()
        observePhotos()
        setupClickListeners()
    }

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

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopDetail.collect { detail ->
                detail ?: return@collect
                binding.tvShopName.text = detail.shopName
                binding.tvShopPhone.text = detail.phone
                binding.tvShopAddress.text =
                    "${detail.address} ${detail.detailAddress}"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopIntro.collect { intro ->
                intro ?: return@collect
                currentIntro = intro.intro
                currentServiceType = intro.serviceChannel
                binding.tvShopIntro.text = intro.intro
                binding.tvShopService.text =
                    if (intro.serviceChannel == "BISIT") "방문 서비스" else "매장 서비스"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.shopAccount.collect { account ->
                account ?: return@collect
                currentAccount = account
                binding.tvSalesAccount.text =
                    "${account.bankName} ${account.accountNumber} ${account.accountHolder}"
            }
        }

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

    private fun setupClickListeners() {

        binding.btnEditShopInfo.setOnClickListener {
            EditShopInfoDialog(
                initialName = binding.tvShopName.text.toString(),
                initialPhone = binding.tvShopPhone.text.toString(),
                initialAddress = binding.tvShopAddress.text.toString(),
                onSaved = { name, phone, addressLine, detailAddress ->
                    viewModel.updateShopBasicInfo(
                        name, phone, addressLine, detailAddress
                    )
                }
            ).show(parentFragmentManager, "edit_shop_info")
        }

        binding.btnEditIntro.setOnClickListener { openIntroDialog() }
        binding.btnChangeHeader.setOnClickListener { openIntroDialog() }

        binding.btnEditSales.setOnClickListener {

            val account = currentAccount ?: return@setOnClickListener

            EditSalesDialog(
                initialAccount = account.accountNumber,
                onResult = { newAccount ->
                    viewModel.updateShopAccount(
                        bankCode = account.bankCode,
                        accountNumber = newAccount,
                        accountHolder = account.accountHolder
                    )
                }
            ).show(parentFragmentManager, "edit_sales")
        }

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

    private fun openIntroDialog() {
        EditShopIntroDialog(
            initialIntro = currentIntro,
            initialServiceType = currentServiceType,
            photoFlow = photoViewModel.photos,
            onAddPhotoClick = { pickImageLauncher.launch("image/*") },
            onDeletePhotoClick = { photoViewModel.deletePhoto(it) },
            onSaved = { intro, serviceType, photos ->
                viewModel.updateShopIntro(
                    intro,
                    serviceType,
                    photos.map { it.id }
                )
            }
        ).show(parentFragmentManager, "edit_intro")
    }

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
