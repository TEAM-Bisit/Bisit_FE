package com.example.bisit.ui.todayReserv

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentTodayReservBinding
import com.example.bisit.ui.todayReserv.dialog.SortOptionDialog

interface SortableFragment {
    fun sort(sortBy: String)   // "recent" | "oldest"
}

class TodayReservFragment : Fragment() {

    private var _binding: FragmentTodayReservBinding? = null
    private val binding get() = _binding!!

    private var isPendingTab = false

    private var currentSortBy: String = "recent"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTabs()
        initSortOption()

        // 초기 화면: Pending 탭
        switchTab(true)
    }

    private fun initTabs() {
        binding.tabPending.setOnClickListener { switchTab(true) }
        binding.tabApproved.setOnClickListener { switchTab(false) }
    }

    private fun switchTab(isPending: Boolean) {
        if (isPendingTab == isPending) return
        isPendingTab = isPending

        val fragment: Fragment =
            if (isPending) PendingReservFragment() else ApprovedReservFragment()

        // 현재 정렬값을 그대로 전달
        fragment.arguments = Bundle().apply {
            putString("sortBy", currentSortBy)
        }

        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()

        updateTabUI(isPending)
    }

    private fun updateTabUI(isPending: Boolean) {
        val active = "#4076FF".toColorInt()
        val inactive = "#9AA1AF".toColorInt()

        binding.tvPending.setTextColor(if (isPending) active else inactive)
        binding.tvApproved.setTextColor(if (!isPending) active else inactive)

        binding.indicatorPending.setBackgroundColor(if (isPending) active else Color.TRANSPARENT)
        binding.indicatorApproved.setBackgroundColor(if (!isPending) active else Color.TRANSPARENT)
    }

    private fun initSortOption() {
        binding.tvSortLabel.setOnClickListener {

            // 문자열 기반 SortOptionDialog 사용
            SortOptionDialog(currentSortBy) { selectedSort ->

                // "recent" 또는 "oldest"
                currentSortBy = selectedSort

                // 표시되는 텍스트 변경
                binding.tvSortLabel.text =
                    if (selectedSort == "recent") "최근 순으로" else "오래된 순으로"

                // 현재 화면 Fragment 에게 정렬 요청
                val currentFragment =
                    childFragmentManager.findFragmentById(binding.fragmentContainer.id)

                if (currentFragment is SortableFragment) {
                    currentFragment.sort(currentSortBy)
                }
            }.show(parentFragmentManager, "sort_option")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
