package com.example.bisit.ui.myPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageBinding

class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)

        // 내 쿠폰 이동
        binding.root.findViewById<View>(R.id.ic_coupon)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCouponFragment)
        }

        // 내 리뷰 이동
        binding.root.findViewById<View>(R.id.ic_review_list)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageReviewFragment)
        }

        // 🔽 로그아웃 클릭 시 AuthFragment로 이동
        binding.root.findViewById<View>(R.id.logout_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_authFragment)
        }

        // 🔽 탈퇴하기 클릭 시 MyPageLeaveFragment로 이동
        binding.root.findViewById<View>(R.id.leave_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageLeaveFragment)
        }

        binding.root.findViewById<View>(R.id.center_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCenterFragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
