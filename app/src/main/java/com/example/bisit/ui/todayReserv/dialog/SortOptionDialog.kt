package com.example.bisit.ui.todayReserv.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.example.bisit.R

class SortOptionDialog(
    private var currentSort: String = "recent",
    private val onSortSelected: ((String) -> Unit)? = null
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_sort_option, container, false)

        val optionRecent = view.findViewById<TextView>(R.id.optionRecent)
        val optionOldest = view.findViewById<TextView>(R.id.optionOldest)
        val btnSave = view.findViewById<AppCompatButton>(R.id.btnSave)

        updateUI(optionRecent, optionOldest)

        // 최근순
        optionRecent.setOnClickListener {
            currentSort = "recent"
            updateUI(optionRecent, optionOldest)
        }

        // 오래된순
        optionOldest.setOnClickListener {
            currentSort = "oldest"
            updateUI(optionRecent, optionOldest)
        }

        btnSave.setOnClickListener {
            onSortSelected?.invoke(currentSort)
            dismiss()
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setLayout(
                (resources.displayMetrics.widthPixels * 0.7583f).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun updateUI(recentView: TextView, oldestView: TextView) {
        val checkBlue = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_blue)
        val checkGray = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_gray)

        val selectedColor = "#4076FF".toColorInt()
        val unselectedColor = "#6D7583".toColorInt()

        val isRecentSelected = currentSort == "recent"

        // recent UI
        recentView.setCompoundDrawablesWithIntrinsicBounds(
            null, null,
            if (isRecentSelected) checkBlue else checkGray,
            null
        )
        recentView.setTextColor(if (isRecentSelected) selectedColor else unselectedColor)

        // oldest UI
        oldestView.setCompoundDrawablesWithIntrinsicBounds(
            null, null,
            if (!isRecentSelected) checkBlue else checkGray,
            null
        )
        oldestView.setTextColor(if (!isRecentSelected) selectedColor else unselectedColor)
    }
}
