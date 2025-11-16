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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icCoupon.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageCouponFragment)
        }

        binding.icReviewList.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageReviewFragment)
        }

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_myPageFragment_to_myPageEditFragment)
        }

        binding.logoutLayout.setOnClickListener {
            findNavController().navigate(
                R.id.authFragment,
                null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
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
}
