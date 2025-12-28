package com.example.bisit.ui.customerPay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.customerReserve.PayCoupon

class CustomerPayCouponFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_customer_pay_coupon, container, false)

        val backBtn = view.findViewById<ImageButton>(R.id.btn_back_coupon)
        backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_coupons)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dummyCoupons = listOf(
            PayCoupon(
                percent = "20%",
                title = "[첫 구매 적용 쿠폰]",
                desc = "첫 구매 20% 할인 쿠폰입니다.",
                dday = "1일 남음",
                date = "2025년 09월 22일까지 사용 가능"
            ),
            PayCoupon(
                percent = "10%",
                title = "[두 번째 구매 적용 쿠폰]",
                desc = "10% 할인 쿠폰입니다.",
                dday = "3일 남음",
                date = "2025년 09월 30일까지 사용 가능"
            )
        )

        val adapter = CustomerPayCouponAdapter(dummyCoupons)
        recyclerView.adapter = adapter

        return view
    }
}
