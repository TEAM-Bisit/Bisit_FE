package com.example.bisit.ui.todayReserv

import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.databinding.FragmentTodayReservBinding
import com.example.bisit.ui.shop.HighlightOverlayView
import com.example.bisit.ui.todayReserv.dialog.SortOptionDialog

interface SortableFragment {
    fun sort(sortBy: String)
}

class TodayReservFragment : Fragment() {

    private var _binding: FragmentTodayReservBinding? = null
    private val binding get() = _binding!!

    private var isPendingTab = false
    private var currentSortBy: String = "recent"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTabs()
        initSortOption()
        switchTab(true)
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

        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        binding.root.post {
            when (activity.currentGuideStep) {

                MainActivity.GuideStep.TODAY_APPROVE -> {
                    val child = childFragmentManager.findFragmentById(binding.fragmentContainer.id)
                    val approveBtn =
                        (child as? TodayApproveTargetProvider)?.getApproveButtonForGuide()

                    showTodayApproveTextAndTail(
                        big = "예약을 승인하면 이곳을 클릭하세요",
                        approveTarget = approveBtn
                    )

                    val rects = mutableListOf<RectF>()
                    rects += rectFOfView(binding.tabContainer)

                    if (approveBtn != null) {
                        rects += rectFOfView(approveBtn)
                    }

                    activity.showGlobalOverlayMultiple(
                        rects = rects,
                        shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                        radiusDp = 16f
                    )
                }

                MainActivity.GuideStep.TODAY_STATUS -> {
                    if (isPendingTab) {
                        switchTab(false)
                        binding.root.post { refreshOnboarding() }
                        return@post
                    }

                    val child = childFragmentManager.findFragmentById(binding.fragmentContainer.id)
                    val statusBtn = (child as? TodayStatusTargetProvider)?.getChangeStatusButtonForGuide()

                    if (statusBtn != null) {
                        // 1) 버튼 하이라이트
                        activity.showGlobalOverlay(
                            targetView = statusBtn,
                            shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                            radiusDp = 16f
                        )

                        // 2) 버튼 위 텍스트(big/small)
                        showTextAboveTarget(
                            targetView = statusBtn,
                            big = "승인내역에서 상태를 변경할 수 있어요",
                            small = "시술 진행 상황을 관리해보세요.",
                            smallTextSizeSp = 14f
                        )
                    }
                }

                MainActivity.GuideStep.TODAY_CONFIRM -> {
                    if (isPendingTab) {
                        switchTab(false)
                        binding.root.post { refreshOnboarding() }
                        return@post
                    }

                    val child = childFragmentManager.findFragmentById(binding.fragmentContainer.id)

                    (child as? ApprovedReservFragment)?.openChangeStatusDialogForOnboardingIfNeeded()

                    showTopFixedText(
                        big = "예약 확정을 해주세요",
                        small = "예약 확정을 하셔야 원활한 서비스 제공이 가능합니다.",
                        smallTextSizeSp = 12f
                    )
                }

                else -> Unit
            }
        }
    }

    private fun showTodayApproveTextAndTail(
        big: String,
        approveTarget: View?
    ) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = TextView(requireContext()).apply {
            text = big
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val tail = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_pig_tail2)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        guideLayer.addView(bigText)
        guideLayer.addView(tail)

        binding.tabContainer.doOnLayout {
            val tabRect = Rect()
            binding.tabContainer.getGlobalVisibleRect(tabRect)

            val layerLoc = IntArray(2)
            guideLayer.getLocationOnScreen(layerLoc)

            val tabBottomLocal = tabRect.bottom - layerLoc[1]
            val left = dp(18f)
            val textTop = tabBottomLocal + dp(20f)

            bigText.x = left
            bigText.y = textTop

            tail.post {
                val tabRightLocal = tabRect.right - layerLoc[0]
                val tabBottomLocal = tabRect.bottom - layerLoc[1]

                tail.measure(
                    View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
                )
                val tailW = tail.measuredWidth.toFloat()

                val rightMargin = dp(18f)
                val tailX = tabRightLocal - tailW - rightMargin

                val tailY = tabBottomLocal - dp(2f)

                tail.x = tailX
                tail.y = tailY

                tail.bringToFront()
                bigText.bringToFront()
            }
        }
    }

    private fun showTextAboveTarget(
        targetView: View,
        big: String,
        small: String,
        smallTextSizeSp: Float
    ) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = TextView(requireContext()).apply {
            text = big
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val smallText = TextView(requireContext()).apply {
            text = small
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSizeSp)
        }

        guideLayer.addView(bigText)
        guideLayer.addView(smallText)

        guideLayer.post {
            val layerLoc = IntArray(2)
            guideLayer.getLocationOnScreen(layerLoc)

            val r = Rect()
            targetView.getGlobalVisibleRect(r)

            val targetTopLocal = r.top - layerLoc[1]

            val left = dp(22f)

            var bigY = targetTopLocal - dp(84f)
            val minY = dp(90f)
            if (bigY < minY) bigY = minY

            bigText.x = left
            bigText.y = bigY

            smallText.x = left
            smallText.y = bigY + dp(28f)
        }
    }

    private fun showTopFixedText(
        big: String,
        small: String,
        smallTextSizeSp: Float
    ) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = TextView(requireContext()).apply {
            text = big
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val smallText = TextView(requireContext()).apply {
            text = small
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, smallTextSizeSp)
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        }

        guideLayer.addView(bigText)
        guideLayer.addView(smallText)

        guideLayer.post {
            val top = dp(140f)

            bigText.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )
            smallText.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )

            val bigW = bigText.measuredWidth.toFloat()
            val smallW = smallText.measuredWidth.toFloat()

            val bigX = (guideLayer.width - bigW) / 2f
            val smallX = (guideLayer.width - smallW) / 2f

            bigText.x = bigX
            bigText.y = top

            smallText.x = smallX
            smallText.y = top + dp(28f)
        }
    }

    private fun rectFOfView(v: View): RectF {
        val r = Rect()
        v.getGlobalVisibleRect(r)
        return RectF(r)
    }

    private fun getGuideLayer(): FrameLayout {
        return (requireActivity() as MainActivity).getGlobalGuideLayer()
    }

    private fun clearGuide() {
        val layer = getGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
        (requireActivity() as MainActivity).hideGlobalOverlay()
    }

    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    // ===== 기존 탭/정렬 로직 =====

    private fun initTabs() {
        binding.tabPending.setOnClickListener { switchTab(true) }
        binding.tabApproved.setOnClickListener { switchTab(false) }
    }

    private fun switchTab(isPending: Boolean) {
        if (isPendingTab == isPending) return
        isPendingTab = isPending

        val fragment: Fragment =
            if (isPending) PendingReservFragment() else ApprovedReservFragment()

        fragment.arguments = Bundle().apply { putString("sortBy", currentSortBy) }

        childFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, fragment)
            .commit()

        updateTabUI(isPending)
    }

    private fun updateTabUI(isPending: Boolean) {
        val active = "#4076FF".toColorInt()
        val inactive = "#9AA1AF".toColorInt()

        binding.tvPending.setTextColor(if (isPending) active else inactive)
        binding.tvApproved.setTextColor(if (!isPending) active else inactive)

        binding.indicatorPending.setBackgroundColor(if (isPending) active else Color.TRANSPARENT)
        binding.indicatorApproved.setBackgroundColor(if (!isPending) active else Color.TRANSPARENT)
    }

    private fun initSortOption() {
        binding.tvSortLabel.setOnClickListener {
            SortOptionDialog(currentSortBy) { selectedSort ->
                currentSortBy = selectedSort
                binding.tvSortLabel.text =
                    if (selectedSort == "recent") "최근 순으로" else "오래된 순으로"

                val currentFragment =
                    childFragmentManager.findFragmentById(binding.fragmentContainer.id)

                if (currentFragment is SortableFragment) {
                    currentFragment.sort(currentSortBy)
                }
            }.show(parentFragmentManager, "sort_option")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}