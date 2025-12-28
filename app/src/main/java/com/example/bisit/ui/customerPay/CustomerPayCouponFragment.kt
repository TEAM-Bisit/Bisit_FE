package com.example.bisit.ui.customerPay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.coupon.ApplicableCouponResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // Fetch applicable coupons
        val treatmentPrice = arguments?.getInt("treatmentPrice") ?: 0
        val memberId = 1L // Placeholder

        RetrofitClient.getCouponApi(requireContext()).getApplicableCoupons(memberId, treatmentPrice)
            .enqueue(object : Callback<ApplicableCouponResponse> {
                override fun onResponse(
                    call: Call<ApplicableCouponResponse>,
                    response: Response<ApplicableCouponResponse>
                ) {
                     if (response.isSuccessful && response.body()?.success == true) {
                        val coupons = response.body()?.data?.coupons ?: emptyList()
                        val adapter = CustomerPayCouponAdapter(coupons) { selectedCoupon ->
                            // Return result to Previous Fragment
                             findNavController().previousBackStackEntry?.savedStateHandle?.set("selectedCoupon", selectedCoupon)
                             findNavController().popBackStack()
                        }
                        recyclerView.adapter = adapter
                     } else {
                         Toast.makeText(context, "적용 가능한 쿠폰 조회 실패", Toast.LENGTH_SHORT).show()
                     }
                }

                override fun onFailure(call: Call<ApplicableCouponResponse>, t: Throwable) {
                     Toast.makeText(context, "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })

        return view
    }
}
