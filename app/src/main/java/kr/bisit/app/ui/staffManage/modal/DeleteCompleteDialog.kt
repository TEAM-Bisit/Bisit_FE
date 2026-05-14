package kr.bisit.app.ui.staffManage.modal

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kr.bisit.app.databinding.DialogDeleteCompleteBinding

class DeleteCompleteDialog(
    private val onClose: (() -> Unit)? = null
) : DialogFragment() {

    private var _binding: DialogDeleteCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogDeleteCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener {
            onClose?.invoke()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val screenWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val wm = requireActivity().windowManager.currentWindowMetrics
                val insets = wm.windowInsets.getInsets(WindowInsets.Type.systemBars())
                wm.bounds.width() - insets.left - insets.right
            } else {
                resources.displayMetrics.widthPixels
            }

            setLayout((screenWidth * 0.806f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
