package com.example.bisit.ui.shop

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShopPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment =
        when (position) {
            0 -> ShopBasicFragment()
            1 -> ShopReviewsFragment()
            2 -> ShopServicesFragment()
            3 -> ShopNoticesFragment()
            else -> throw IllegalStateException()
        }
}
