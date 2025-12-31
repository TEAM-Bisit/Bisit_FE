package com.example.bisit.ui.myPageOwner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageOwnerBinding
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.auth.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        binding.logoutLayout.setOnClickListener {
            performLogout()
        }

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageOwnerEditFragment)
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
