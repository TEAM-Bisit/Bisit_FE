package kr.bisit.app.ui.staffManage.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import kr.bisit.app.ui.staffManage.StaffListFragment
import kr.bisit.app.ui.staffManage.StaffRequestsFragment

class StaffPagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> StaffRequestsFragment()
        else -> StaffListFragment()
    }
}
