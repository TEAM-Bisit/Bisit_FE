package com.example.bisit.ui.auth

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.bisit.databinding.DialogUserTypeBinding

class UserTypeDialog : DialogFragment() {

    private var _binding: DialogUserTypeBinding? = null
    private val binding get() = _binding!!

    private var listener: UserTypeDialogListener? = null

    interface UserTypeDialogListener {
        fun onUserTypeSelected(userType: String)
    }

    fun setListener(listener: UserTypeDialogListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogUserTypeBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOwner.setOnClickListener {
            listener?.onUserTypeSelected("owner")
            dismiss()
        }

        binding.btnCustomer.setOnClickListener {
            listener?.onUserTypeSelected("customer")
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): UserTypeDialog {
            return UserTypeDialog()
        }
    }
}