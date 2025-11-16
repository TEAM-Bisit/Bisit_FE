package com.example.bisit.ui.myPageOwner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageOwnerBinding

class MyPageOwnerFragment : Fragment() {
    private var _binding: FragmentMyPageOwnerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageOwnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageOwnerEditFragment)
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
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageLeaveFragment)
        }

        binding.centerLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageCenterFragment)
        }

        binding.announceLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageAnnounceFragment)
        }

        binding.term1.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm1Fragment)
        }

        binding.term2.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm2Fragment)
        }

        binding.term3.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm3Fragment)
        }

        binding.term4.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm4Fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
