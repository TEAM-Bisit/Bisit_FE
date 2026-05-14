package kr.bisit.app.ui.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kr.bisit.app.databinding.DialogCustomTwoButtonBinding

class CustomTwoButtonDialog(
    private val title: String,
    private val subtitle: String,
    private val positiveButtonText: String = "확인",
    private val negativeButtonText: String = "취소",
    private val onPositiveClick: () -> Unit,
    private val onNegativeClick: (() -> Unit)? = null
) : DialogFragment() {

    private var _binding: DialogCustomTwoButtonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogCustomTwoButtonBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTitle.text = title
        binding.tvSubtitle.text = subtitle
        binding.btnPositive.text = positiveButtonText
        binding.btnNegative.text = negativeButtonText

        binding.btnPositive.setOnClickListener {
            onPositiveClick()
            dismiss()
        }

        binding.btnNegative.setOnClickListener {
            onNegativeClick?.invoke()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}