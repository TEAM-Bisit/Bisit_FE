package com.example.bisit.ui.customerShop

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
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

        shopId = arguments?.getLong("shopId") ?: 1L

        binding.rvShopDetail.layoutManager = LinearLayoutManager(requireContext())
        adapter = CustomerShopDetailAdapter(emptyList(), emptyList(), emptyList())
        binding.rvShopDetail.adapter = adapter

        binding.shopBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnBook.setOnClickListener {
            findNavController().navigate(R.id.action_customerShopFragment_to_shopDesignerFragment)
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

        viewModel.loadShop(requireContext(), shopId)
        viewModel.loadShopIntroduce(requireContext(), shopId)
    }

    private fun renderShop(
        data: com.example.bisit.data.model.customerShop.CustomerShopDetailItem?,
        introData: com.example.bisit.data.model.customerShop.CustomerShopIntroduceData?,
        noticeRel: String?
    ) {
        if (data == null) return

        val weeklyList = data.weeklyBusinessHours?.mapNotNull { convertBusinessHourToString(it) } ?: emptyList()

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

        val services = listOf<List<ServiceItem>>(
            emptyList()
        )

        val reviews = listOf<List<ReviewItem>>(
            emptyList()
        )

        adapter = CustomerShopDetailAdapter(listOf(shopDetailItem), services, reviews)
        binding.rvShopDetail.adapter = adapter
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
