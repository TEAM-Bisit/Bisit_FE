package com.example.bisit.ui.shop

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ShopPagerAdapter(parent: Fragment) : FragmentStateAdapter(parent) {
    override fun getItemCount(): Int = 4
    override fun createFragment(position: Int) = when (position) {
        0 -> ShopBasicFragment()
        1 -> ShopReviewsFragment()
        2 -> ShopServicesFragment()
        else -> ShopNoticesFragment()
    }
}