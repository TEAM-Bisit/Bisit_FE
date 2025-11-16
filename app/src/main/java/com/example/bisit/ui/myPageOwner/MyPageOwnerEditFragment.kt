package com.example.bisit.ui.myPageOwner

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageOwnerEditBinding

class MyPageOwnerEditFragment : Fragment() {

    private var _binding: FragmentMyPageOwnerEditBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageOwnerEditBinding.inflate(inflater, container, false)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        setupCameraDialog()
        setupSaveDialog()
        setupPhoneInput()

        return binding.root
    }

    private fun setupCameraDialog() {
        binding.icCamera.setOnClickListener {
            showDialog(R.layout.dialog_my_page_owner_edit)
        }
    }

    private fun setupSaveDialog() {
        binding.btnBook.setOnClickListener {
            showDialog(R.layout.dialog_my_page_owner_edit_store)
        }
    }

    private fun showDialog(layoutId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutId)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    private fun setupPhoneInput() {
        val phoneEt = binding.etPhone
        val verifyBtn = binding.btnVerify

        phoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = s.toString().trim().isNotEmpty()

                if (hasText) {
                    verifyBtn.isEnabled = true
                    verifyBtn.setBackgroundResource(R.drawable.bg_my_page_phone_active)
                } else {
                    verifyBtn.isEnabled = false
                    verifyBtn.setBackgroundResource(R.drawable.bg_my_page_phone)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        verifyBtn.setOnClickListener {
            binding.etPhone2.visibility = View.VISIBLE
            binding.btnVerify2.visibility = View.VISIBLE
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
