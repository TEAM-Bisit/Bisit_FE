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

        binding.root.findViewById<View>(R.id.logout_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_authFragment)
        }

        binding.root.findViewById<View>(R.id.leave_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageLeaveFragment)
        }

        binding.root.findViewById<View>(R.id.center_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageCenterFragment)
        }

        binding.root.findViewById<View>(R.id.announce_layout)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageAnnounceFragment)
        }

        binding.root.findViewById<View>(R.id.term1)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm1Fragment)
        }

        binding.root.findViewById<View>(R.id.term2)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm2Fragment)
        }

        binding.root.findViewById<View>(R.id.term3)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm3Fragment)
        }

        binding.root.findViewById<View>(R.id.term4)?.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm4Fragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
