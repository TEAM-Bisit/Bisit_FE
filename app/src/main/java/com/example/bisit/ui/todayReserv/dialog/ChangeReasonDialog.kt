package com.example.bisit.ui.todayReserv.dialog

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
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.bisit.R

class ChangeReasonDialog(
    private val onRejectConfirmed: (() -> Unit)? = null
) : DialogFragment() {

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

        btnClose.setOnClickListener {
            dismiss()
        }

        etReason.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                tvCharCount.text = "${input.length}/50자"

                btnSubmit.isEnabled = input.length >= 8
                btnSubmit.setTextColor(
                    if (btnSubmit.isEnabled) Color.WHITE
                    else ContextCompat.getColor(requireContext(), R.color.gray)
                )
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSubmit.setOnClickListener {
            dismiss()

            RejectCompleteDialog().show(parentFragmentManager, "reject_complete")

            onRejectConfirmed?.invoke()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val metrics = requireActivity().windowManager.currentWindowMetrics
                val insets = metrics.windowInsets.getInsets(WindowInsets.Type.systemBars())
                metrics.bounds.width() - insets.left - insets.right
            } else {
                @Suppress("DEPRECATION")
                resources.displayMetrics.widthPixels
            }

            val width = (screenWidth * 0.806f).toInt()
            setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }
}
