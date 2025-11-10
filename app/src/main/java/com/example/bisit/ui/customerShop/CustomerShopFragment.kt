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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.bisit.data.model.shop.ServiceItem
import com.example.bisit.data.model.shop.ReviewItem
import com.example.bisit.data.model.shop.ShopDetailItem
import com.example.bisit.databinding.FragmentShopBinding
import com.example.bisit.R

class CustomerShopFragment : Fragment() {
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyDetailList = listOf(
            ShopDetailItem(
                name = "장미헤어",
                category = "미용실 · 뷰티케어",
                review = "리뷰 9개",
                rating = "4.8",
                summary = "샵 한 줄 소개입니다. 한줄소개. 샵 한 줄 소개입니다. 한줄소개",
                address = "대구 중구 관덕정길 6-11 1층",
                openInfo = "영업중 09:00 ~ 18:00",
                phone = "010-0000-0000",
                notice = "내일은 휴무입니다",
                noticeTime = "4시간 전",
                weeklyOpenHours = listOf(
                    "월 09:00 ~ 18:00",
                    "화 09:00 ~ 18:00",
                    "수 09:00 ~ 18:00",
                    "목 09:00 ~ 18:00",
                    "금 09:00 ~ 18:00",
                    "토 09:00 ~ 18:00",
                    "일 휴무"
                )
            )
        )

        val services = listOf(
            listOf(
                ServiceItem("모니터 수리", "모니터 브랜드 모두 취급합니다.", "60분", "50,000원"),
                ServiceItem("열펌", "뿌리 볼륨을 살릴 수 있는 펌", "70분", "80,000원"),
                ServiceItem("볼륨매직", "방문 서비스 가능", "80분", "100,000원")
            )
        )

        val reviews = listOf(
            listOf(
                ReviewItem("김승현", "아이맥도 잘 고쳐주셨어요. 방문해주셔서 덕분에 잘 사용 중입니다. ㅠㅠ", "2025.09.12"),
                ReviewItem("임*형", "오늘도 마음에 듭니다~ 최고세요", "2025.09.12")
            )
        )

        binding.rvShopDetail.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShopDetail.adapter = CustomerShopDetailAdapter(dummyDetailList, services, reviews)

        binding.shopBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnBook.setOnClickListener {
            findNavController().navigate(R.id.action_shopFragment_to_shopDesignerFragment)
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

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
