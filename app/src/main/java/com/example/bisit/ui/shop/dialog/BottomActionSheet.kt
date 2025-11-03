package com.example.bisit.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bisit.R
import com.example.bisit.databinding.SheetActionsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomActionSheet : BottomSheetDialogFragment() {
    companion object {
        const val REQUEST_KEY = "bottom_action_sheet"
        const val RESULT_ACTION = "action"
        const val ACTION_DELETE = "delete"
        const val ACTION_EDIT = "edit"
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetTheme

    private var _b: SheetActionsBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = SheetActionsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.btnDelete.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putString(RESULT_ACTION, ACTION_DELETE) }
            )
            dismiss()
        }
        b.btnEdit.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putString(RESULT_ACTION, ACTION_EDIT) }
            )
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val white = "#FEFEFE".toColorInt()

            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.setDimAmount(0.3f)

            @Suppress("DEPRECATION")
            window.navigationBarColor = white

            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightNavigationBars = true

            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
