package kr.bisit.app.ui.shop

import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.repository.staffManage.StaffManageRepository
import kr.bisit.app.databinding.FragmentShopBinding
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

    override fun onResume() {
        super.onResume()
        refreshOnboarding()
    }


    fun refreshOnboarding() {

        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide()
            return
        }

        binding.root.post {

            when (activity.currentGuideStep) {

                MainActivity.GuideStep.TAB -> {

                    activity.showGlobalOverlay(
                        targetView = binding.tabContainer,
                        shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                        radiusDp = 16f
                    )

                    showTextBelow(
                        targetView = binding.tabContainer,
                        big = "고객님들에게 보이는\n정보들을 관리할 수 있어요",
                        small = "리뷰 관리와 공지사항 등록 등을 관리해보세요.",
                        bottomMarginDp = 20f
                    )
                }

                MainActivity.GuideStep.SERVICE_TAB -> {

                    activity.showGlobalOverlay(
                        targetView = binding.tabServices,
                        shape = HighlightOverlayView.HighlightShape.CIRCLE,
                        radiusDp = 50f
                    )

                    showTextBelowFixedLeft(
                        targetView = binding.tabServices,
                        big = "안녕하세요 사장님!\n우선 우리 가게 서비스를 등록해볼까요?",
                        small = "밝은 곳을 터치해서 매장 시술을 등록해보세요.",
                        bottomMarginDp = 24f,
                        leftMarginDp = 18f
                    )
                }

                MainActivity.GuideStep.SERVICE_SCREEN -> {
                    binding.viewPager.currentItem = 2
                    clearGuide()
                }

                MainActivity.GuideStep.TODAY_TAB -> {

                    activity.highlightBottomNavItem(index = 1)

                    val rect = activity.getBottomNavHighlightRect(index = 1)
                    if (rect != null) {
                        showTodayTabGuideWithPigTail(
                            highlightRect = rect,
                            big = "이곳에서 오늘 들어온 예약을 볼 수 있어요."
                        )
                    } else {
                        clearGuide()
                    }
                }

                else -> clearGuide()
            }
        }
    }

    /* ================= Guide Layer ================= */

    private fun getGuideLayer(): FrameLayout {
        return (requireActivity() as MainActivity).getGlobalGuideLayer()
    }

    private fun clearGuide() {
        val layer = getGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
    }

    private fun dp(value: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )
    }

    private fun createBigText(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun createSmallText(text: String): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
    }

    private fun showTextBelow(
        targetView: View,
        big: String,
        small: String,
        bottomMarginDp: Float
    ) {

        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = createBigText(big)
        val smallText = createSmallText(small)

        guideLayer.addView(bigText)
        guideLayer.addView(smallText)

        bigText.post {

            val rect = Rect()
            targetView.getGlobalVisibleRect(rect)

            val layerLocation = IntArray(2)
            guideLayer.getLocationOnScreen(layerLocation)

            val left = rect.left - layerLocation[0] + dp(4f)
            val top = rect.bottom - layerLocation[1] + dp(bottomMarginDp)

            bigText.x = left
            bigText.y = top

            smallText.x = left
            smallText.y = top + dp(64f)
        }
    }

    private fun showTodayTabGuideWithPigTail(
        highlightRect: RectF,
        big: String,
        tailToTextGapDp: Float = 10f
    ) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val tail = ImageView(requireContext()).apply {
            setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pig_tail))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val bigText = createBigText(big)

        guideLayer.addView(tail)
        guideLayer.addView(bigText)

        // guideLayer가 레이아웃 된 다음에 좌표 계산
        guideLayer.post {

            val layerLocation = IntArray(2)
            guideLayer.getLocationOnScreen(layerLocation)

            // highlightRect(스크린 좌표) -> guideLayer 로컬 좌표로 변환
            val cx = highlightRect.centerX() - layerLocation[0]
            val topOfCircle = highlightRect.top - layerLocation[1]

            val gap = dp(tailToTextGapDp)

            // tail 위치: 원의 top에 "바로 붙여" 올림 (tail의 bottom == 원 top)
            tail.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )
            val tailW = tail.measuredWidth.toFloat()
            val tailH = tail.measuredHeight.toFloat()

            var tailX = cx - tailW / 2f
            var tailY = topOfCircle - tailH

            // 텍스트 위치: tail 위로 10dp
            bigText.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )
            val textW = bigText.measuredWidth.toFloat()
            val textH = bigText.measuredHeight.toFloat()

            var textX = cx - textW / 2f
            var textY = tailY - gap - textH

            // 화면 밖으로 안 나가게 clamp (최소 18dp 정도 여백)
            val margin = dp(18f)
            val maxXForTail = guideLayer.width - tailW - margin
            val maxXForText = guideLayer.width - textW - margin

            tailX = tailX.coerceIn(margin, maxXForTail)
            textX = textX.coerceIn(margin, maxXForText)

            // Y도 너무 위로 가면 조금 내려줌(원하는대로 조절 가능)
            val minY = margin
            if (textY < minY) {
                // textY가 너무 위면 tail/text를 같이 아래로 내림
                val delta = (minY - textY)
                textY += delta
                tailY += delta
            }

            tail.x = tailX
            tail.y = tailY

            bigText.x = textX
            bigText.y = textY

            tail.bringToFront()
            bigText.bringToFront()
        }
    }

    private fun showTextBelowFixedLeft(
        targetView: View,
        big: String,
        small: String,
        bottomMarginDp: Float,
        leftMarginDp: Float = 18f
    ) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = createBigText(big)
        val smallText = createSmallText(small)

        guideLayer.addView(bigText)
        guideLayer.addView(smallText)

        bigText.post {
            val rect = Rect()
            targetView.getGlobalVisibleRect(rect)

            val layerLocation = IntArray(2)
            guideLayer.getLocationOnScreen(layerLocation)

            val fixedLeft = dp(leftMarginDp)

            val top = rect.bottom - layerLocation[1] + dp(bottomMarginDp)

            bigText.x = fixedLeft
            bigText.y = top

            smallText.x = fixedLeft
            smallText.y = top + dp(64f)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupStaffRequestViewModel()
        setupViewPagerAndTabs()
        setupStaffApplyNavigation()
        observeShopIdForBadgeOnly()
        observeStaffRequestState()

        updateTabUI(0)
    }

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
