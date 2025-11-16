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
            val hasText = !text.isNullOrEmpty()

            if (!isVerifyActive) {
                binding.btnVerify.isEnabled = hasText
            }
        }

        binding.btnVerify.setOnClickListener {
            isVerifyActive = !isVerifyActive

            if (isVerifyActive) {
                binding.btnVerify.isEnabled = true
                binding.etPhone2.visibility = View.VISIBLE
                binding.btnVerify2.visibility = View.VISIBLE
            } else {
                binding.btnVerify.isEnabled = false
                binding.etPhone2.visibility = View.GONE
                binding.btnVerify2.visibility = View.GONE
                binding.etPhone2.text.clear()
            }
        }
        binding.etPhone2.addTextChangedListener { text ->
            binding.btnVerify2.isEnabled = !text.isNullOrEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
