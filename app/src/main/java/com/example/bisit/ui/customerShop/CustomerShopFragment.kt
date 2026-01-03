package com.example.bisit.ui.customerShop

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.data.model.customerShop.BusinessHourItem
import com.example.bisit.data.model.shop.ServiceItem
import com.example.bisit.data.model.shop.ShopDetailItem
import com.example.bisit.data.repository.customerShop.CustomerShopRepository
import com.example.bisit.databinding.FragmentCustomerShopBinding

class CustomerShopFragment : Fragment() {

    private var _binding: FragmentCustomerShopBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CustomerShopViewModel by viewModels {
        CustomerShopViewModelFactory(CustomerShopRepository(requireContext()))
    }

    private lateinit var adapter: CustomerShopDetailAdapter
    private var shopId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 상태바 높이만큼 padding 추가
        applyStatusBarPadding()

        shopId = arguments?.getLong("shopId") ?: -1L
        if (shopId == -1L) {
            Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding.rvShopDetail.layoutManager = LinearLayoutManager(requireContext())
        adapter = CustomerShopDetailAdapter(emptyList(), emptyList(), emptyList()) {}
        binding.rvShopDetail.adapter = adapter

        binding.shopBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.shopHome.setOnClickListener {
            findNavController().navigate(R.id.customerCategoryFragment)
        }
        binding.btnShare.setOnClickListener {
            val shopData = viewModel.shopData.value
            if (shopData != null) {
                val shareBody = "[${shopData.shopName}] 매장 정보를 확인해보세요!\n\n" +
                        "주소: ${shopData.address} ${shopData.detailAddress ?: ""}\n" +
                        "전화번호: ${shopData.phone ?: "정보 없음"}\n\n" +
                        "나오때(Naottae) 앱에서 더 자세한 정보를 확인하세요."
                
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_SUBJECT, "[나오때] 매장 공유")
                    putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                }
                startActivity(android.content.Intent.createChooser(intent, "매장 정보 공유하기"))
            } else {
                Toast.makeText(requireContext(), "정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBook.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("shopId", shopId)
                putString("shopName", viewModel.shopData.value?.shopName ?: "")
            }
            findNavController().navigate(R.id.action_customerShopFragment_to_shopDesignerFragment, bundle)
        }

        viewModel.shopData.observe(viewLifecycleOwner) { data ->
            renderShop(data, viewModel.introduceData.value, viewModel.noticeRelativeTime.value)
        }

        viewModel.introduceData.observe(viewLifecycleOwner) { intro ->
            renderShop(viewModel.shopData.value, intro, viewModel.noticeRelativeTime.value)
        }

        viewModel.noticeRelativeTime.observe(viewLifecycleOwner) { rel ->
            renderShop(viewModel.shopData.value, viewModel.introduceData.value, rel)
        }

        viewModel.errorMsg.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.servicesData.observe(viewLifecycleOwner) { services ->
            Log.d("CustomerShopFragment", "servicesData observer: ${services.size} services")
            val reviews = viewModel.reviewsData.value ?: emptyList()
            if (::adapter.isInitialized) {
                adapter.updateData(services, reviews)
            }
        }

        viewModel.reviewsData.observe(viewLifecycleOwner) { reviews ->
            Log.d("CustomerShopFragment", "reviewsData observer: ${reviews.size} reviews")
            val services = viewModel.servicesData.value ?: emptyList()
            if (::adapter.isInitialized) {
                adapter.updateData(services, reviews)
            }
        }

        Log.d("CustomerShopFragment", "Starting to load shop with id: $shopId")
        viewModel.loadShop(requireContext(), shopId)
        viewModel.loadShopIntroduce(requireContext(), shopId)
        viewModel.loadShopServices(requireContext(), shopId)
        viewModel.loadShopReviews(requireContext(), shopId)
    }

    private fun applyStatusBarPadding() {
        val statusBarHeight = getStatusBarHeight()
        binding.coordinatorLayout.setPadding(0, statusBarHeight, 0, 0)
        
        // topBar에도 marginTop 적용
        val topBarParams = binding.topBar.layoutParams as android.widget.FrameLayout.LayoutParams
        topBarParams.topMargin = statusBarHeight
        binding.topBar.layoutParams = topBarParams
        
        Log.d("CustomerShopFragment", "Applied status bar padding: $statusBarHeight px")
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun renderShop(
        data: com.example.bisit.data.model.customerShop.CustomerShopDetailItem?,
        introData: com.example.bisit.data.model.customerShop.CustomerShopIntroduceData?,
        noticeRel: String?
    ) {
        Log.d("CustomerShopFragment", "renderShop called - data: $data, introData: $introData, noticeRel: $noticeRel")
        if (data == null) {
            Log.w("CustomerShopFragment", "data is null, returning")
            return
        }

        val weeklyList = data.weeklyBusinessHours?.mapNotNull { convertBusinessHourToString(it) } ?: emptyList()
        Log.d("CustomerShopFragment", "weeklyList size: ${weeklyList.size}")

        val shopDetailItem = com.example.bisit.data.model.customerShop.CustomerShopUiItem(
            name = data.shopName ?: "",
            category = data.category ?: "",
            review = "리뷰 ${data.reviewCount ?: 0}개",
            rating = (data.averageRating?.toString() ?: "0.0"),
            summary = data.shortIntro ?: "",
            address = "${data.address ?: ""} ${data.detailAddress ?: ""}".trim(),
            openInfo = data.todayBusinessHours?.replace(" ~ ", "~") ?: "",
            phone = data.phone ?: "",
            notice = data.latestNotice?.title ?: "",
            noticeTime = noticeRel ?: "",
            weeklyOpenHours = weeklyList,
            intro = introData?.intro,
            photos = introData?.photos?.map { it.url }
        )

        Log.d("CustomerShopFragment", "shopDetailItem created: ${shopDetailItem.name}")

        // Get current services and reviews data
        val services = viewModel.servicesData.value ?: emptyList()
        val reviews = viewModel.reviewsData.value ?: emptyList()

        // Create or update adapter
        if (!::adapter.isInitialized) {
            adapter = CustomerShopDetailAdapter(listOf(shopDetailItem), listOf(services), listOf(reviews)) {
                navigateToMoreReviews()
            }
            binding.rvShopDetail.adapter = adapter
            Log.d("CustomerShopFragment", "Adapter initialized with ${adapter.itemCount} items, ${services.size} services, ${reviews.size} reviews")
        } else {
            // If adapter already exists, we need to recreate with new shop data
            adapter = CustomerShopDetailAdapter(listOf(shopDetailItem), listOf(services), listOf(reviews)) {
                navigateToMoreReviews()
            }
            binding.rvShopDetail.adapter = adapter
            Log.d("CustomerShopFragment", "Adapter recreated with updated shop data")
        }
    }

    private fun navigateToMoreReviews() {
        try {
            val bundle = Bundle().apply {
                putLong("shopId", shopId)
            }
            findNavController().navigate(R.id.action_customerShopFragment_to_customerShopMoreReviewFragment, bundle)
        } catch (e: Exception) {
            Log.e("CustomerShopFragment", "Nav Error", e)
            Toast.makeText(requireContext(), "화면 이동 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertBusinessHourToString(item: BusinessHourItem): String? {
        val dayKr = when (item.day) {
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            "SUNDAY" -> "일"
            else -> item.day ?: ""
        }

        return if (item.isClosed == true) {
            "$dayKr 휴무"
        } else {
            // Parse time format from HH:mm:ss to HH:mm
            val openFrom = item.openFrom?.substring(0, 5) ?: ""
            val openTo = item.openTo?.substring(0, 5) ?: ""
            "$dayKr $openFrom~$openTo"
        }
    }

    private fun showCopyDialog(address: String) {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_copy_address, null)
        dialog.setContentView(view)

        val tvAddress = view.findViewById<TextView>(R.id.tvAddress)
        val btnCopy = view.findViewById<TextView>(R.id.btnCopy)
        val btnClose = view.findViewById<TextView>(R.id.btnClose)

        tvAddress.text = address

        btnCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("주소", address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
