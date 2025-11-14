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

        binding.root.findViewById<View>(R.id.ic_coupon)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCouponFragment)
        }

        binding.root.findViewById<View>(R.id.ic_review_list)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageReviewFragment)
        }

        binding.root.findViewById<View>(R.id.logout_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_authFragment)
        }

        binding.root.findViewById<View>(R.id.leave_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageLeaveFragment)
        }

        binding.root.findViewById<View>(R.id.center_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCenterFragment)
        }

        binding.root.findViewById<View>(R.id.announce_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageAnnounceFragment)
        }

        binding.root.findViewById<View>(R.id.term1)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm1Fragment)
        }

        binding.root.findViewById<View>(R.id.term2)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm2Fragment)
        }

        binding.root.findViewById<View>(R.id.term3)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm3Fragment)
        }

        binding.root.findViewById<View>(R.id.term4)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageTerm4Fragment)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
