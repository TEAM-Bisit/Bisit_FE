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
    }

    override fun getTheme(): Int = R.style.CustomBottomSheetTheme

    private var _b: SheetActionsBinding? = null
    private val b get() = _b!!

    /**
     * BottomSheet 자체 설정
     * - FULL HEIGHT
     * - 위쪽만 둥근 배경 적용
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)

        dialog.setOnShowListener {
            val bottomSheet =
                dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) ?: return@setOnShowListener

            // 전체 높이
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            // 둥근 배경은 여기서만 적용
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

    /**
     * Window를 edge-to-edge로
     * (하지만 배경은 투명)
     */
    override fun onStart() {
        super.onStart()

        dialog?.window?.apply {
            val white = "#FEFEFE".toColorInt()

            // Window 자체는 투명
            setBackgroundDrawableResource(android.R.color.transparent)
            setDimAmount(0.3f)

            // 네비게이션 바 색상
            @Suppress("DEPRECATION")
            navigationBarColor = white

            WindowInsetsControllerCompat(this, decorView).apply {
                isAppearanceLightNavigationBars = true
            }

            // 시스템 바 영역까지 확장
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
