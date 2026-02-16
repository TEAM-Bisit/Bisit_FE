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
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.repository.staffManage.StaffManageRepository
import com.example.bisit.databinding.FragmentShopBinding
import kotlinx.coroutines.launch

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
    }

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
        setupStaffApplyNavigation()
        observeShopIdForBadgeOnly()
        observeStaffRequestState()

        updateTabUI(0)

        // 🔥 온보딩 체크
        binding.root.post {
            checkOnboardingStep()
        }
    }

    /* ===================== 온보딩 처리 ===================== */

    private fun checkOnboardingStep() {

        val activity = requireActivity() as MainActivity

        when (activity.currentGuideStep) {

            MainActivity.GuideStep.TAB -> {
                activity.showGlobalOverlay(
                    targetView = binding.tabContainer,
                    guideText = "탭을 눌러 화면을 이동할 수 있어요.",
                    shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                    radiusDp = 8f
                )
            }

            MainActivity.GuideStep.EDIT_BUTTON -> {
                activity.showGlobalOverlay(
                    targetView = binding.viewPager,
                    guideText = "수정 버튼을 눌러 매장 정보를 바꿀 수 있어요.",
                    shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                    radiusDp = 12f
                )
            }

            MainActivity.GuideStep.SERVICE_TAB -> {
                activity.showGlobalOverlay(
                    targetView = binding.tabServices,
                    guideText = "서비스 등록 탭에서 서비스를 추가해보세요.",
                    shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                    radiusDp = 12f
                )
            }

            else -> {
                activity.hideGlobalOverlay()
            }
        }
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

    private fun setupStaffApplyNavigation() {
        binding.ivStaffApply.setOnClickListener {
            findNavController().navigate(
                R.id.action_shopFragment_to_staffManagementFragment
            )
        }
    }

    /* ===================== observe ===================== */

    private fun observeShopIdForBadgeOnly() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                if (shopId != null) {
                    staffRequestViewModel.checkPendingStaffExists(shopId)
                }
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
