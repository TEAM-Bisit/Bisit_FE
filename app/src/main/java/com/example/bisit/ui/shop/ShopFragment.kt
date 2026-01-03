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

    /** 직원 신청 상태 ViewModel (뱃지 표시용) */
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

        setupStaffRequestViewModel()
        setupViewPagerAndTabs()
        setupStaffApplyNavigation()     // ⭐️ 무조건 이동
        observeShopIdForBadgeOnly()     // ⭐️ 뱃지 표시만
        observeStaffRequestState()

        updateTabUI(0)
    }

    /* ===================== setup ===================== */

    private fun setupStaffRequestViewModel() {
        val api = RetrofitClient.getStaffManageApi(requireContext())
        val repository = StaffManageRepository(api)

        staffRequestViewModel = ViewModelProvider(
            this,
            ShopStaffRequestViewModelFactory(repository)
        )[ShopStaffRequestViewModel::class.java]
    }

    private fun setupViewPagerAndTabs() {
        binding.viewPager.adapter = ShopPagerAdapter(this)

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

    /**
     * 직원 신청 페이지는 조건 없이 항상 이동 가능
     */
    private fun setupStaffApplyNavigation() {
        binding.ivStaffApply.setOnClickListener {
            findNavController().navigate(
                R.id.action_shopFragment_to_staffManagementFragment
            )
        }
    }

    /* ===================== observe ===================== */

    /**
     * shopId는 '새 직원 신청 있음/없음' 뱃지 표시 용도
     * 네비게이션 / 접근 제어와는 무관
     */
    private fun observeShopIdForBadgeOnly() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                if (shopId != null) {
                    staffRequestViewModel.checkPendingStaffExists(shopId)
                }
                // shopId 없어도 아무것도 막지 않음
            }
        }
    }

    private fun observeStaffRequestState() {
        viewLifecycleOwner.lifecycleScope.launch {
            staffRequestViewModel.state.collect { state ->
                if (state.hasPendingRequest) {
                    binding.ivStaffApply.setImageResource(R.drawable.ic_staff_new_apply)
                    binding.ivStaffApplyBubble.visibility = View.VISIBLE
                } else {
                    binding.ivStaffApply.setImageResource(R.drawable.ic_staff_apply)
                    binding.ivStaffApplyBubble.visibility = View.GONE
                }
            }
        }
    }

    /* ===================== UI ===================== */

    private fun updateTabUI(position: Int) {
        val inactive = "#9AA1AF".toColorInt()
        val active = "#6D7583".toColorInt()
        val indicator = "#4076FF".toColorInt()

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
        ).forEach { it.setTextColor(inactive) }

        when (position) {
            0 -> {
                binding.indicatorBasic.setBackgroundColor(indicator)
                binding.tvBasic.setTextColor(active)
            }
            1 -> {
                binding.indicatorReviews.setBackgroundColor(indicator)
                binding.tvReviews.setTextColor(active)
            }
            2 -> {
                binding.indicatorServices.setBackgroundColor(indicator)
                binding.tvServices.setTextColor(active)
            }
            3 -> {
                binding.indicatorNotices.setBackgroundColor(indicator)
                binding.tvNotices.setTextColor(active)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
