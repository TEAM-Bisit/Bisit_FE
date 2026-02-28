package com.example.bisit.ui.myPageOwner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentOwnerCouponManageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.bisit.R
import com.example.bisit.databinding.SheetOwnerCouponMoreBinding

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bisit.MainActivity
import com.example.bisit.data.model.coupon.CreateCouponRequest
import com.example.bisit.data.model.coupon.OwnerCouponItem
import com.example.bisit.data.model.coupon.UpdateCouponRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerCouponManageFragment : Fragment() {

    private var _binding: FragmentOwnerCouponManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OwnerCouponAdapter
    private val viewModel: OwnerCouponViewModel by viewModels {
        OwnerCouponViewModelFactory(requireContext())
    }

    private val shopId: Long = 1L // TODO: Get actual shopId from session/pref

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOwnerCouponManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        viewModel.fetchCoupons(shopId)
    }

    override fun onResume() {
        super.onResume()
        refreshOnboarding()
    }

    fun refreshOnboarding() {
        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide(activity)
            return
        }

        if (activity.currentGuideStep != MainActivity.GuideStep.MY_COUPON) return

        binding.btnAddCoupon.post {
            activity.showGlobalOverlay(
                targetView = binding.btnAddCoupon,
                shape = com.example.bisit.ui.shop.HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                radiusDp = 16f
            )

            showBigTextAboveTarget(
                targetView = binding.btnAddCoupon,
                big = "쿠폰 추가하기를 눌러\n우리 매장의 쿠폰을 생성할 수 있어요."
            )
        }
    }

    private fun showBigTextAboveTarget(targetView: View, big: String) {
        val activity = requireActivity() as MainActivity
        val guideLayer = activity.getGlobalGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = android.widget.TextView(requireContext()).apply {
            text = big
            setTextColor(android.graphics.Color.WHITE)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        guideLayer.addView(bigText)

        guideLayer.post {
            val layerLoc = IntArray(2)
            guideLayer.getLocationOnScreen(layerLoc)

            val r = android.graphics.Rect()
            targetView.getGlobalVisibleRect(r)

            val targetTopLocal = r.top - layerLoc[1]
            val left = dp(22f)
            val margin = dp(12f)

            bigText.measure(
                View.MeasureSpec.makeMeasureSpec(guideLayer.width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(guideLayer.height, View.MeasureSpec.AT_MOST)
            )
            val textH = bigText.measuredHeight.toFloat()

            var y = targetTopLocal - margin - textH

            val minY = dp(16f)
            if (y < minY) y = minY

            bigText.x = left
            bigText.y = y
            bigText.bringToFront()
        }
    }

    private fun clearGuide(activity: MainActivity) {
        val layer = activity.getGlobalGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
        activity.hideGlobalOverlay()
    }

    private fun dp(value: Float): Float =
        android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )

    private fun setupRecyclerView() {
        adapter = OwnerCouponAdapter { coupon ->
            showMoreBottomSheet(coupon)
        }
        binding.rvCoupons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCoupons.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddCoupon.setOnClickListener {
            showAddCouponDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.coupons.collectLatest { coupons ->
                adapter.submitList(coupons)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                // Show/hide loading indicator if exists
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    // Show error toast
                    viewModel.clearError()
                }
            }
        }
    }

    private fun showAddCouponDialog() {
        val dialog = DialogAddCoupon(requireContext()) { newCouponRequest ->
            if (newCouponRequest is CreateCouponRequest) {
                viewModel.createCoupon(shopId, newCouponRequest)
            }
        }
        dialog.show()
    }

    private fun showMoreBottomSheet(coupon: OwnerCouponItem) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val bindingSheet = SheetOwnerCouponMoreBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.btnDelete.setOnClickListener {
            viewModel.deleteCoupon(coupon.couponId, shopId)
            bottomSheet.dismiss()
        }

        bindingSheet.btnEdit.setOnClickListener {
            bottomSheet.dismiss()
            showEditCouponDialog(coupon)
        }

        bottomSheet.show()
    }

    private fun showEditCouponDialog(coupon: OwnerCouponItem) {
        val dialog = DialogAddCoupon(requireContext(), coupon) { updatedCouponRequest ->
            if (updatedCouponRequest is UpdateCouponRequest) {
                viewModel.updateCoupon(coupon.couponId, shopId, updatedCouponRequest)
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
