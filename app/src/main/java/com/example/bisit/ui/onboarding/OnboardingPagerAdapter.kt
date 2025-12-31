package com.example.bisit.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.bisit.R

class OnboardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnboardingPageFragment.newInstance(R.drawable.img_onboarding_1)
            1 -> OnboardingPageFragment.newInstance(R.drawable.img_onboarding_2)
            else -> OnboardingPageFragment.newInstance(R.drawable.img_onboarding_3)
        }
    }
}