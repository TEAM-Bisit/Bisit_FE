package com.example.naottae.ui.todayReserv

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.naottae.databinding.FragmentTodayReservBinding
import com.example.naottae.ui.todayReserv.dialog.SortOptionDialog

interface SortableFragment {
    fun sort(isRecent: Boolean)
}

class TodayReservFragment : Fragment() {
    private var _binding: FragmentTodayReservBinding? = null
    private val binding get() = _binding!!

    private var isPendingTab = false
    private var isRecentSort = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabs()
        initSortOption()

        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, PendingReservFragment())
            .commit()

        switchTab(true)
    }

    private fun initTabs() {
        binding.tabPending.setOnClickListener {
            switchTab(true)
        }
        binding.tabApproved.setOnClickListener {
            switchTab(false)
        }
    }

    private fun switchTab(isPending: Boolean) {
        if (isPendingTab == isPending) return
        isPendingTab = isPending

        val fragment = if (isPending) PendingReservFragment() else ApprovedReservFragment()
        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()

        val active = "#FE6B6B".toColorInt()
        val inactive = "#9AA1AF".toColorInt()

        binding.tvPending.setTextColor(if (isPending) active else inactive)
        binding.tvApproved.setTextColor(if (isPending) inactive else active)
        binding.indicatorPending.setBackgroundColor(if (isPending) active else Color.TRANSPARENT)
        binding.indicatorApproved.setBackgroundColor(if (isPending) Color.TRANSPARENT else active)
    }

    private fun initSortOption() {
        binding.tvSortLabel.setOnClickListener {
            SortOptionDialog(isRecent = isRecentSort) { selected ->
                isRecentSort = selected
                binding.tvSortLabel.text =
                    if (selected) "최근 순으로" else "오래된 순으로"

                val currentFragment = childFragmentManager.findFragmentById(binding.fragmentContainer.id)
                if (currentFragment is SortableFragment) {
                    currentFragment.sort(isRecentSort)
                }
            }.show(parentFragmentManager, "sort_option")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
