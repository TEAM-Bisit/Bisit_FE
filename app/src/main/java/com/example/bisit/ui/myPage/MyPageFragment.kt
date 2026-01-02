package com.example.bisit.ui.myPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageBinding
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.auth.AuthResponse
import com.example.bisit.data.model.member.MyPageResponse
import com.example.bisit.data.model.member.MyPageData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch My Page Info
        fetchMyPageInfo()

        binding.logoutLayout.setOnClickListener {
            performLogout()
        }

        binding.icCoupon.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCouponFragment)
        }

        binding.icReviewList.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageReviewFragment)
        }

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageEditFragment)
        }

        binding.leaveLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageLeaveFragment)
        }

        binding.centerLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCenterFragment)
        }

        binding.announceLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageAnnounceFragment)
        }

        binding.term1.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm1Fragment)
        }

        binding.term2.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm2Fragment)
        }

        binding.term3.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm3Fragment)
        }

        binding.term4.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm4Fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun fetchMyPageInfo() {
        RetrofitClient.getMemberApi(requireContext()).getMyPage()
            .enqueue(object : Callback<MyPageResponse> {
                override fun onResponse(
                    call: Call<MyPageResponse>,
                    response: Response<MyPageResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        binding.tvName.text = data?.name ?: "사용자"
                        binding.tvCouponCnt.text = "${data?.couponCount ?: 0}"
                        binding.tvReviewCnt.text = "${data?.reviewCount ?: 0}"
                    }
                }

                override fun onFailure(call: Call<MyPageResponse>, t: Throwable) {
                    // Handle failure
                }
            })
    }

    private fun performLogout() {
        RetrofitClient.getAuthApi(requireContext()).logout()
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.logout()
                    } else {
                        Toast.makeText(requireContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
