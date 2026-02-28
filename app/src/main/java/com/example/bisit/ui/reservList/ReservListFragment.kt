package com.example.bisit.ui.reservList

import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.data.model.reservList.ReservationListItem
import com.example.bisit.databinding.FragmentReservListBinding
import com.example.bisit.ui.reservList.adapter.ReservListAdapter
import com.example.bisit.ui.reservList.dialog.ReservListCalendarDialog
import com.example.bisit.ui.shop.HighlightOverlayView
import com.example.bisit.ui.shop.ShopRegisterViewModel
import com.example.bisit.ui.shop.ShopRegisterViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ReservListFragment : Fragment() {

    private var _binding: FragmentReservListBinding? = null
    private val binding get() = _binding!!

    /** 예약 리스트 ViewModel */
    private val reservListViewModel: ReservListViewModel by viewModels()

    /** shopId 공유 ViewModel (Activity 범위) */
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    private lateinit var adapter: ReservListAdapter
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    private var isAscending = true

    private var detailBtnForGuide: View? = null

    private var retryCount = 0
    private val MAX_RETRY = 20

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        reservListViewModel.setOnboardingMode(activity.isOnboardingActive())

        /** RecyclerView 설정 */
        adapter = ReservListAdapter(emptyList()) { selected ->
            val bundle = Bundle().apply {
                putLong("reservationId", selected.reservationId)
            }
            findNavController().navigate(
                R.id.action_reservList_to_detail,
                bundle
            )
        }

        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter

        observeShopId()
        observeReservationList()

        /** 달력 버튼 */
        binding.btnCalendar.setOnClickListener {
            ReservListCalendarDialog { _ ->
                // 날짜 필터 적용 시
            }.show(childFragmentManager, "CalendarDialog")
        }

        /** 정렬 버튼 */
        binding.btnAscending.setOnClickListener {
            if (!isAscending) {
                isAscending = true
                updateButtonUI()
                sortList()
            }
        }

        binding.btnDescending.setOnClickListener {
            if (isAscending) {
                isAscending = false
                updateButtonUI()
                sortList()
            }
        }

        updateButtonUI()
    }

    override fun onResume() {
        super.onResume()
        retryCount = 0

        val activity = requireActivity() as MainActivity
        if (activity.isOnboardingActive()) {
            reservListViewModel.loadReservationList(
                shopId = 1L,
                isRefresh = true
            )
        }

        refreshOnboarding()
    }

    /* ===================== Onboarding (TODAY_DETAIL) ===================== */

    fun refreshOnboarding() {
        val activity = requireActivity() as MainActivity
        android.util.Log.d("ONBOARD", "ReservList refresh: step=${activity.currentGuideStep}, enabled=${activity.isOnboardingActive()}")

        if (!activity.isOnboardingActive()) {
            clearGuide(activity)
            return
        }

        if (activity.currentGuideStep != MainActivity.GuideStep.TODAY_DETAIL) return

        binding.rvReservations.scrollToPosition(0)

        if (detailBtnForGuide == null) {
            captureDetailButtonFromFirstItem()
        }

        binding.root.post {
            val navRect = activity.getBottomNavHighlightRect(index = 2)
            val detailBtn = detailBtnForGuide

            if (detailBtn == null) {
                captureDetailButtonFromFirstItem()
            }

            if (navRect == null || detailBtn == null) {
                if (retryCount < MAX_RETRY) {
                    retryCount++
                    binding.root.postDelayed({ refreshOnboarding() }, 120)
                }
                return@post
            }

            activity.showGlobalOverlayMixed(
                specs = listOf(
                    HighlightOverlayView.HighlightSpec(
                        rect = rectFOfView(detailBtn),
                        shape = HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                        radiusPx = dp(12f)
                    ),
                    HighlightOverlayView.HighlightSpec(
                        rect = navRect,
                        shape = HighlightOverlayView.HighlightShape.CIRCLE,
                        radiusPx = 0f
                    )
                )
            )

            showTodayDetailGuideTextsAndTail(
                highlightRect = navRect,
                detailTarget = detailBtn,
                topBig = "상세보기를 눌러\n고객님의 정보를 확인할 수 있어요.",
                bottomBig = "승인하신 예약을 포함한 모든 예약을\n예약 내역에서 확인할 수 있어요."
            )
        }
    }

    private fun captureDetailButtonFromFirstItem() {
        binding.rvReservations.post {
            val vh = binding.rvReservations.findViewHolderForAdapterPosition(0)

            if (vh == null) {
                binding.rvReservations.postDelayed({
                    val vh2 = binding.rvReservations.findViewHolderForAdapterPosition(0)
                    detailBtnForGuide = vh2?.itemView?.findViewById(R.id.btnDetail)
                }, 80)
                return@post
            }

            detailBtnForGuide = vh.itemView.findViewById(R.id.btnDetail)
        }
    }

    private fun showTodayDetailGuideTextsAndTail(
        highlightRect: RectF,
        detailTarget: View,
        topBig: String,
        bottomBig: String
    ) {
        val activity = requireActivity() as MainActivity
        val guideLayer = activity.getGlobalGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val topText = TextView(requireContext()).apply {
            text = topBig
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tail = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_pig_tail)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val bottomText = TextView(requireContext()).apply {
            text = bottomBig
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        guideLayer.addView(topText)
        guideLayer.addView(tail)
        guideLayer.addView(bottomText)

        guideLayer.post {
            val layerLoc = IntArray(2)
            guideLayer.getLocationOnScreen(layerLoc)

            val left = dp(18f)

            // (A) 상세보기 버튼 위 텍스트
            val r = Rect()
            detailTarget.getGlobalVisibleRect(r)
            val targetTopLocal = r.top - layerLoc[1]

            var topY = targetTopLocal - dp(86f)
            val minY = dp(90f)
            if (topY < minY) topY = minY

            topText.x = left
            topText.y = topY

            // (B) 바텀네비 원 + 꼬리 + 텍스트
            val circleCxLocal = highlightRect.centerX() - layerLoc[0]
            val circleTopLocal = highlightRect.top - layerLoc[1]

            tail.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )
            bottomText.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )

            val tailW = tail.measuredWidth.toFloat()
            val tailH = tail.measuredHeight.toFloat()

            val tailX = circleCxLocal - tailW / 2f
            val tailY = circleTopLocal - tailH

            tail.x = tailX
            tail.y = tailY

            bottomText.x = left
            bottomText.y = tailY - dp(12f) - bottomText.measuredHeight

            tail.bringToFront()
            bottomText.bringToFront()
            topText.bringToFront()
        }
    }

    private fun rectFOfView(v: View): RectF {
        val r = Rect()
        v.getGlobalVisibleRect(r)
        return RectF(r)
    }

    private fun clearGuide(activity: MainActivity) {
        val layer = activity.getGlobalGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
        activity.hideGlobalOverlay()
    }

    private fun dp(value: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    /* ===================== 기존 데이터/정렬 로직 ===================== */

    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                val activity = requireActivity() as MainActivity
                if (activity.isOnboardingActive()) {
                    return@repeatOnLifecycle
                }

                shopRegisterViewModel.shopId.collect { shopId ->
                    shopId ?: return@collect
                    reservListViewModel.loadReservationList(
                        shopId = shopId,
                        isRefresh = true
                    )
                }
            }
        }
    }

    private fun observeReservationList() {
        reservListViewModel.reservationList.observe(viewLifecycleOwner) { list ->
            sortAndSubmit(list)
        }
    }

    private fun sortList() {
        reservListViewModel.reservationList.value?.let {
            sortAndSubmit(it)
        }
    }

    private fun sortAndSubmit(list: List<ReservationListItem>) {
        val sortedList = if (isAscending) {
            list.sortedBy { dateFormat.parse("${it.reservedDate} ${it.startTime}") }
        } else {
            list.sortedByDescending { dateFormat.parse("${it.reservedDate} ${it.startTime}") }
        }

        adapter.updateList(sortedList)

        // ✅ 리스트 갱신 후 첫 아이템 버튼 다시 캡처 + 온보딩 갱신
        detailBtnForGuide = null
        captureDetailButtonFromFirstItem()
        retryCount = 0
        refreshOnboarding()
    }

    private fun updateButtonUI() {
        if (isAscending) {
            binding.btnAscending.setBackgroundResource(R.drawable.bg_order_active)
            binding.btnAscending.setTextColor(Color.WHITE)

            binding.btnDescending.setBackgroundResource(R.drawable.bg_order_inactive)
            binding.btnDescending.setTextColor("#222222".toColorInt())
        } else {
            binding.btnDescending.setBackgroundResource(R.drawable.bg_order_active)
            binding.btnDescending.setTextColor(Color.WHITE)

            binding.btnAscending.setBackgroundResource(R.drawable.bg_order_inactive)
            binding.btnAscending.setTextColor("#222222".toColorInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}