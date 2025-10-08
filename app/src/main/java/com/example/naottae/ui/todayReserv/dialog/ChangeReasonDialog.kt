package com.example.naottae.ui.todayReserv.dialog

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.naottae.R

class ChangeReasonDialog : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_reason_input, container, false)

        val etReason = view.findViewById<EditText>(R.id.etReason)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmit)
        val tvCharCount = view.findViewById<TextView>(R.id.tvCharCount)
        val btnClose = view.findViewById<ImageView>(R.id.btnClose)

        // 🔸 X 버튼 클릭 → 모달 닫기
        btnClose.setOnClickListener {
            dismiss()
        }

        etReason.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                tvCharCount.text = "${input.length}/50자"

                if (input.length >= 8) {
                    btnSubmit.isEnabled = true
                    btnSubmit.setTextColor(Color.WHITE)
                } else {
                    btnSubmit.isEnabled = false
                    val disabledTextColor = ContextCompat.getColor(requireContext(), R.color.gray)
                    btnSubmit.setTextColor(disabledTextColor)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnSubmit.setOnClickListener {
            val text = etReason.text.toString()
            Toast.makeText(requireContext(), "입력한 사유: $text", Toast.LENGTH_SHORT).show()
            dismiss()
            RejectCompleteDialog().show(parentFragmentManager, "reject_complete")
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = requireActivity().windowManager.currentWindowMetrics
                val insets = windowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                windowMetrics.bounds.width() - insets.left - insets.right
            } else {
                @Suppress("DEPRECATION")
                val displayMetrics = resources.displayMetrics
                @Suppress("DEPRECATION")
                displayMetrics.widthPixels
            }

            val width = (screenWidth * 0.806f).toInt()
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            setLayout(width, height)
        }
    }
}
