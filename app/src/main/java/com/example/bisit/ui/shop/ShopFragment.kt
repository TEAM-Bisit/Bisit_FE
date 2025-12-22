package com.example.bisit.ui.shop

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.bisit.databinding.FragmentShopBinding

class ShopFragment : Fragment() {
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Assuming ShopFragment receives shopId from arguments
        val shopId = arguments?.getLong("shopId") ?: -1L 
        // Note: Check if ShopFragment receives "shopId" in nav graph or caller.
        // Based on CustomerShopFragment, it navigates to "shopDesignerFragment" not "shopFragment" directly visible.
        // Wait, where is ShopFragment used?
        // If ShopFragment is the one with tabs, maybe IT IS the destination?
        // CustomerShopFragment seems to show details in a list.
        // I will assume ShopFragment is used correctly somewhere and args are passed.
        val pagerAdapter = ShopPagerAdapter(this, shopId)
        binding.viewPager.adapter = pagerAdapter

        // 각 탭 클릭 시 페이지 전환
        binding.tabBasic.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.tabReviews.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.tabServices.setOnClickListener { binding.viewPager.currentItem = 2 }
        binding.tabNotices.setOnClickListener { binding.viewPager.currentItem = 3 }

        // ViewPager 슬라이드 시 indicator / 색상 업데이트
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabUI(position)
            }
        })

        // 초기 상태 (기본 탭 활성화)
        updateTabUI(0)
    }

    private fun updateTabUI(position: Int) {
        val inactiveColor = "#9AA1AF".toColorInt()
        val activeColor = "#6D7583".toColorInt()
        val indicatorColor = "#4076FF".toColorInt()

        listOf(
            binding.indicatorBasic,
            binding.indicatorReviews,
            binding.indicatorServices,
            binding.indicatorNotices
        ).forEach { it.setBackgroundColor(Color.TRANSPARENT) }

        listOf(
            binding.tvBasic,
            binding.tvReviews,
            binding.tvServices,
            binding.tvNotices
        ).forEach { it.setTextColor(inactiveColor) }

        // 현재 선택된 탭만 활성화
        when (position) {
            0 -> {
                binding.indicatorBasic.setBackgroundColor(indicatorColor)
                binding.tvBasic.setTextColor(activeColor)
            }
            1 -> {
                binding.indicatorReviews.setBackgroundColor(indicatorColor)
                binding.tvReviews.setTextColor(activeColor)
            }
            2 -> {
                binding.indicatorServices.setBackgroundColor(indicatorColor)
                binding.tvServices.setTextColor(activeColor)
            }
            3 -> {
                binding.indicatorNotices.setBackgroundColor(indicatorColor)
                binding.tvNotices.setTextColor(activeColor)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
