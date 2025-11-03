package com.example.bisit.ui.staffManage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.bisit.databinding.FragmentStaffManagementBinding
import com.example.bisit.ui.staffManage.adapter.StaffPagerAdapter

class StaffManagementFragment : Fragment() {

    private var _binding: FragmentStaffManagementBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffManagementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = StaffPagerAdapter(this)
        binding.viewPagerStaff.adapter = pagerAdapter

        binding.tabRequests.setOnClickListener { binding.viewPagerStaff.currentItem = 0 }
        binding.tabList.setOnClickListener { binding.viewPagerStaff.currentItem = 1 }

        binding.viewPagerStaff.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabUI(position)
            }
        })

        updateTabUI(0)

        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateTabUI(position: Int) {
        val inactiveColor = "#515965".toColorInt()
        val activeColor = "#222222".toColorInt()
        val indicatorColor = "#4076FF".toColorInt()

        listOf(binding.indicatorRequests, binding.indicatorList)
            .forEach { it.setBackgroundColor(Color.TRANSPARENT) }

        listOf(binding.tvRequests, binding.tvList)
            .forEach { it.setTextColor(inactiveColor) }

        when (position) {
            0 -> {
                binding.indicatorRequests.setBackgroundColor(indicatorColor)
                binding.tvRequests.setTextColor(activeColor)
            }
            1 -> {
                binding.indicatorList.setBackgroundColor(indicatorColor)
                binding.tvList.setTextColor(activeColor)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
