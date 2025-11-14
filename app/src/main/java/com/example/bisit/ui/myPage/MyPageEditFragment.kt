package com.example.bisit.ui.myPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageEditBinding

class MyPageEditFragment : Fragment() {

    private var _binding: FragmentMyPageEditBinding? = null
    private val binding get() = _binding!!

    private var isVerifyActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etPhone.addTextChangedListener { text ->
            if (!text.isNullOrEmpty() && !isVerifyActive) {
                binding.btnVerify.setBackgroundResource(R.drawable.bg_my_page_phone_active)
            } else if (!isVerifyActive) {
                binding.btnVerify.setBackgroundResource(R.drawable.bg_my_page_phone)
            }
        }

        binding.btnVerify.setOnClickListener {
            if (!isVerifyActive) {
                isVerifyActive = true
                binding.btnVerify.setBackgroundResource(R.drawable.bg_my_page_phone_active)
                binding.etCode.visibility = View.VISIBLE
                binding.btnDone.visibility = View.VISIBLE
            } else {
                isVerifyActive = false
                binding.btnVerify.setBackgroundResource(R.drawable.bg_my_page_phone)
                binding.etCode.visibility = View.GONE
                binding.btnDone.visibility = View.GONE
                binding.etCode.text.clear()
            }
        }

        binding.etCode.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.btnDone.setBackgroundResource(R.drawable.bg_my_page_phone_active)
            } else {
                binding.btnDone.setBackgroundResource(R.drawable.bg_my_page_phone)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
