package kr.bisit.app.ui.myPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.coupon.CouponListResponse
import kr.bisit.app.databinding.FragmentMyPageCouponBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        
        fetchCoupons()

        binding.btnBackCoupon.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun fetchCoupons() {
        // TODO: Use real memberId. For now using placeholder 1L as per plan.
        val memberId = 1L
        
        RetrofitClient.getCouponApi(requireContext()).getMyCoupons(memberId, 0, 100)
            .enqueue(object : Callback<CouponListResponse> {
                override fun onResponse(
                    call: Call<CouponListResponse>,
                    response: Response<CouponListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val coupons = response.body()?.data?.coupons?.content ?: emptyList()
                        binding.rvCoupons.adapter = MyPageCouponAdapter(coupons)
                    } else {
                        // Handle error
                    }
                }

                override fun onFailure(call: Call<CouponListResponse>, t: Throwable) {
                    // Handle failure
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
