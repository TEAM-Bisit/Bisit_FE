package com.example.bisit.ui.myPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentMyPageCouponBinding

class MyPageCouponFragment : Fragment() {

    private var _binding: FragmentMyPageCouponBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageCouponBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCoupons.layoutManager = LinearLayoutManager(requireContext())
        val dummyCoupons = listOf(
            mapOf(
                "percent" to "20%",
                "title" to "[첫 구매 적용 쿠폰]",
                "desc" to "첫 구매 20% 할인 쿠폰입니다.",
                "dday" to "1일 남음",
                "validDate" to "2025년 09월 22일까지 사용 가능"
            ),
            mapOf(
                "percent" to "10%",
                "title" to "[두 번째 쿠폰]",
                "desc" to "두 번째 쿠폰 설명입니다.",
                "dday" to "3일 남음",
                "validDate" to "2025년 10월 01일까지 사용 가능"
            )
        )
        binding.rvCoupons.adapter = MyPageCouponAdapter(dummyCoupons)

        binding.btnBackCoupon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
