package com.example.bisit.ui.staffManage.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bisit.ui.staffManage.StaffListFragment
import com.example.bisit.ui.staffManage.StaffRequestsFragment

class StaffPagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> StaffRequestsFragment()
        else -> StaffListFragment()
    }
}
