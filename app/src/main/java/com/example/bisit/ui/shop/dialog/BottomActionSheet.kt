package com.example.bisit.ui.shop.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowInsetsControllerCompat
import com.example.bisit.R
import com.example.bisit.databinding.SheetActionsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomActionSheet : BottomSheetDialogFragment() {

    companion object {
        const val REQUEST_KEY = "bottom_action_sheet"
        const val RESULT_ACTION = "action"

        const val ACTION_DELETE = "delete"
        const val ACTION_EDIT = "edit"

        // 사용 타입
        private const val ARG_TYPE = "type"
        const val TYPE_REVIEW = "review"
        const val TYPE_OTHER = "other"

        fun newInstance(type: String): BottomActionSheet {
            return BottomActionSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_TYPE, type)
                }
            }
        }
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetTheme

    private var _b: SheetActionsBinding? = null
    private val b get() = _b!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) ?: return@setOnShowListener

            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet.setBackgroundResource(R.drawable.bg_bottom_sheet_top_round)
            bottomSheet.clipToOutline = true

            BottomSheetBehavior.from(bottomSheet).apply {
                state = BottomSheetBehavior.STATE_EXPANDED
                skipCollapsed = true
                isDraggable = true
            }
        }

        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _b = SheetActionsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val type = arguments?.getString(ARG_TYPE)

        // 리뷰일 때만 삭제 버튼 노출
        if (type == TYPE_REVIEW) {
            b.btnDelete.visibility = View.VISIBLE
            b.btnEdit.visibility = View.GONE
        } else {
            b.btnDelete.visibility = View.GONE
            b.btnEdit.visibility = View.GONE
        }

        b.btnDelete.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                REQUEST_KEY,
                Bundle().apply { putString(RESULT_ACTION, ACTION_DELETE) }
            )
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            val white = "#FEFEFE".toColorInt()

            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.3f)

            @Suppress("DEPRECATION")
            navigationBarColor = white

            WindowInsetsControllerCompat(this, decorView).apply {
                isAppearanceLightNavigationBars = true
            }

            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
