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
import com.example.bisit.data.model.shop.ReviewItem
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

        shopId = arguments?.getLong("shopId") ?: 3L

        binding.rvShopDetail.layoutManager = LinearLayoutManager(requireContext())
        adapter = CustomerShopDetailAdapter(emptyList(), emptyList(), emptyList())
        binding.rvShopDetail.adapter = adapter

        binding.shopBack.setOnClickListener { parentFragmentManager.popBackStack() }
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

        Log.d("CustomerShopFragment", "Starting to load shop with id: $shopId")
        viewModel.loadShop(requireContext(), shopId)
        viewModel.loadShopIntroduce(requireContext(), shopId)
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
            openInfo = data.todayBusinessHours ?: "",
            phone = data.phone ?: "",
            notice = data.latestNotice?.title ?: "",
            noticeTime = noticeRel ?: "",
            weeklyOpenHours = weeklyList,
            intro = introData?.intro,
            photos = introData?.photos?.map { it.url }
        )

        Log.d("CustomerShopFragment", "shopDetailItem created: ${shopDetailItem.name}")

        val services = listOf<List<ServiceItem>>(
            emptyList()
        )

        val reviews = listOf<List<ReviewItem>>(
            emptyList()
        )

        adapter = CustomerShopDetailAdapter(listOf(shopDetailItem), services, reviews)
        binding.rvShopDetail.adapter = adapter
        Log.d("CustomerShopFragment", "Adapter set with ${adapter.itemCount} items")
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
            val openFrom = item.openFrom ?: ""
            val openTo = item.openTo ?: ""
            val breakPart = if (!item.breakFrom.isNullOrBlank() && !item.breakTo.isNullOrBlank()) {
                " (브레이크 ${item.breakFrom} ~ ${item.breakTo})"
            } else ""
            "$dayKr $openFrom ~ $openTo$breakPart"
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
