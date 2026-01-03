package com.example.bisit.ui.shop

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.repository.staffManage.StaffManageRepository
import com.example.bisit.databinding.FragmentShopBinding
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    /** shopId 단일 소스 (Activity scope) */
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
    }

    /** 직원 신청 상태 ViewModel (Fragment scope) */
    private lateinit var staffRequestViewModel: ShopStaffRequestViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 직원 신청 ViewModel 초기화 */
        val api = RetrofitClient.getStaffManageApi(requireContext())
        val repository = StaffManageRepository(api)

        staffRequestViewModel = ViewModelProvider(
            this,
            ShopStaffRequestViewModelFactory(repository)
        )[ShopStaffRequestViewModel::class.java]

        /** shopId 관찰 (도착 시점에만 초기화) */
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                if (shopId == null) return@collect

                setupShopWithId(shopId)
            }
        }

        /** 직원 신청 상태 관찰 → 아이콘 / 말풍선 반영 */
        viewLifecycleOwner.lifecycleScope.launch {
            staffRequestViewModel.state.collect { state ->
                if (state.hasPendingRequest) {
                    binding.ivStaffApply.setImageResource(
                        R.drawable.ic_staff_new_apply
                    )
                    binding.ivStaffApplyBubble.visibility = View.VISIBLE
                } else {
                    binding.ivStaffApply.setImageResource(
                        R.drawable.ic_staff_apply
                    )
                    binding.ivStaffApplyBubble.visibility = View.GONE
                }
            }
        }

        updateTabUI(0)
    }

    /**
     * shopId가 준비된 이후에만 호출되는 초기화 로직
     */
    private fun setupShopWithId(shopId: Long) {

        /** Shop 진입 시 직원 신청 존재 여부 확인 */
        staffRequestViewModel.checkPendingStaffExists(shopId)

        /** 직원 신청 버튼 클릭 → StaffRequestsFragment 이동 */
        binding.ivStaffApply.setOnClickListener {
            val bundle = Bundle().apply {
                putLong("shopId", shopId)
            }
            findNavController().navigate(
                R.id.action_shopFragment_to_staffRequestsFragment,
                bundle
            )
        }

        /** ViewPager 설정 */
        val pagerAdapter = ShopPagerAdapter(this, shopId)
        binding.viewPager.adapter = pagerAdapter

        binding.tabBasic.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.tabReviews.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.tabServices.setOnClickListener { binding.viewPager.currentItem = 2 }
        binding.tabNotices.setOnClickListener { binding.viewPager.currentItem = 3 }

        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateTabUI(position)
                }
            }
        )
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
