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
import com.example.bisit.databinding.FragmentMyPageLeaveBinding
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.auth.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageLeaveFragment : Fragment() {

    private var _binding: FragmentMyPageLeaveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageLeaveBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnLeave.setOnClickListener {
            performWithdrawal()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performWithdrawal() {
        RetrofitClient.getAuthApi(requireContext()).withdraw()
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.logout()
                    } else {
                        Toast.makeText(requireContext(), "회원탈퇴 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
