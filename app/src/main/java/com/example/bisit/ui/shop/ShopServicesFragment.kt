package com.example.bisit.ui.shop

import android.graphics.Rect
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.MainActivity
import com.example.bisit.databinding.FragmentShopServicesBinding
import com.example.bisit.ui.shop.adapter.ServiceAdapter
import com.example.bisit.ui.shop.dialog.AddServiceDialog
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import com.example.bisit.data.model.shop.TreatmentResponse
import com.example.bisit.ui.shop.model.toRequest
import com.example.bisit.util.uriToMultipart
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import android.graphics.Typeface

class ShopServicesFragment : Fragment() {

    private var _binding: FragmentShopServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ServiceAdapter

    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
    }

    private val shopServiceViewModel: ShopServiceViewModel by activityViewModels {
        ShopServiceViewModelFactory(requireContext())
    }

    private var shopId: Long? = null

    private fun getGuideLayer(): ViewGroup {
        return (requireActivity() as MainActivity).getGlobalGuideLayer()
    }

    /* ===================== Lifecycle ===================== */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeShopId()
        observeServiceList()
        observeError()

        // ✅ 일반 동작: 사용자가 그냥 눌러도 모달 열리게 유지
        binding.fabAdd.setOnClickListener {
            openAddServiceDialogNormal()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshOnboarding()
    }

    /* ===================== RecyclerView ===================== */

    private fun setupRecyclerView() {
        adapter = ServiceAdapter(
            onMoreClick = { item ->
                showActionSheet(item)
            }
        )
        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServices.adapter = adapter
    }

    /* ===================== shopId 구독 ===================== */

    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collectLatest { id ->
                id?.let {
                    shopId = it
                    shopServiceViewModel.loadTreatments(shopId = it, isFirst = true)
                }
            }
        }
    }

    /* ===================== 서비스 목록 구독 ===================== */

    private fun observeServiceList() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopServiceViewModel.treatments.collectLatest { list ->
                adapter.submitList(list)
            }
        }
    }

    /* ===================== 에러 처리 ===================== */

    private fun observeError() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopServiceViewModel.errorMessage.collectLatest { message ->
                message?.let {
                    // Toast / Snackbar
                }
            }
        }
    }

    /* ===================== 액션 시트 ===================== */

    private fun showActionSheet(item: TreatmentResponse) {
        BottomActionSheet().show(parentFragmentManager, "actions")

        parentFragmentManager.setFragmentResultListener(
            BottomActionSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(BottomActionSheet.RESULT_ACTION)) {
                BottomActionSheet.ACTION_DELETE -> showDeleteConfirm(item)
                BottomActionSheet.ACTION_EDIT -> openEditServiceDialog(item)
            }
        }
    }

    /* ===================== 서비스 삭제 ===================== */

    private fun showDeleteConfirm(item: TreatmentResponse) {
        ConfirmDialog(
            message = "서비스를 삭제하시겠어요?",
            onConfirm = {
                shopId?.let {
                    shopServiceViewModel.deleteTreatment(
                        treatmentId = item.treatmentId,
                        shopId = it
                    )
                }
            }
        ).show(parentFragmentManager, "confirm_delete")
    }

    /* ===================== 서비스 추가/수정(기존 로직 유지) ===================== */

    private fun openAddServiceDialogNormal() {
        AddServiceDialog(
            onSaved = { treatment, imageUri ->
                val photoPart: MultipartBody.Part? =
                    imageUri?.let { uriToMultipart(requireContext(), it, "photo") }

                shopId?.let {
                    shopServiceViewModel.createTreatment(
                        shopId = it,
                        request = treatment.toRequest(),
                        photo = photoPart
                    )
                }
            }
        ).show(parentFragmentManager, "add_service")
    }

    private fun openEditServiceDialog(item: TreatmentResponse) {
        AddServiceDialog(
            prefill = item,
            onSaved = { updated, imageUri ->
                val photoPart: MultipartBody.Part? =
                    imageUri?.let { uriToMultipart(requireContext(), it, "photo") }

                shopId?.let {
                    shopServiceViewModel.updateTreatment(
                        treatmentId = updated.treatmentId,
                        shopId = it,
                        request = updated.toRequest(),
                        photo = photoPart
                    )
                }
            }
        ).show(parentFragmentManager, "edit_service")
    }

    /* ===================== Onboarding ===================== */

    fun refreshOnboarding() {
        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide()
            return
        }

        binding.fabAdd.post {
            when (activity.currentGuideStep) {

                // 1) FAB 동그라미 + "이곳을 누르세요!"
                MainActivity.GuideStep.SERVICE_SCREEN -> {
                    activity.showGlobalOverlay(
                        targetView = binding.fabAdd,
                        shape = HighlightOverlayView.HighlightShape.CIRCLE,
                        radiusDp = 40f
                    )
                    showGuideTextAboveFab(binding.fabAdd, "이곳을 누르세요!")
                }

                // 2) 모달 뜨기 직전 안내(텍스트만, 상단 160dp / left 18dp)
                MainActivity.GuideStep.SERVICE_MODAL_GUIDE -> {
                    activity.showDimOnlyOverlay() // 딤 + 버튼 유지 (구멍 없음)
                    showModalGuideTextTop(
                        big = "본격적으로 시술을 등록해볼까요?",
                        small = "더 쾌적한 이용을 위해 모든 입력창을 채워주세요."
                    )
                }

                // 3) 안내 끝 → 오버레이/텍스트 제거 → 모달 실제 오픈
                MainActivity.GuideStep.SERVICE_MODAL_OPEN -> {
                    activity.hideGlobalOverlay()
                    clearGuide()
                    openAddServiceDialogForOnboarding() // ✅ 닫히면 TODAY_TAB로
                }

                else -> {
                    // 다른 단계는 여기서 관여 X (필요하면 정리만)
                }
            }
        }
    }

    // SERVICE_SCREEN 텍스트(기존 동작 유지)
    private fun showGuideTextAboveFab(targetView: View, text: String) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val guideText = TextView(requireContext()).apply {
            this.text = text
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        guideLayer.addView(guideText)

        guideLayer.post {
            guideText.post {
                val rect = Rect()
                targetView.getGlobalVisibleRect(rect)

                val layerLocation = IntArray(2)
                guideLayer.getLocationOnScreen(layerLocation)

                val margin18 = dp(18f)

                val fabLeft = rect.left - layerLocation[0]
                val fabTop = rect.top - layerLocation[1]

                var x = fabLeft + (targetView.width / 2f) - (guideText.width / 2f)
                var y = fabTop - guideText.height - margin18

                val minX = margin18
                val maxX = (guideLayer.width - guideText.width - margin18).toFloat()
                val minY = margin18
                val maxY = (guideLayer.height - guideText.height - margin18).toFloat()

                if (maxX >= minX) x = x.coerceIn(minX, maxX)
                if (maxY >= minY) y = y.coerceIn(minY, maxY)

                guideText.x = x
                guideText.y = y
                guideText.bringToFront()
            }
        }
    }

    // SERVICE_MODAL_GUIDE: 상단 고정 텍스트(동그라미 없음)
    private fun showModalGuideTextTop(big: String, small: String) {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = TextView(requireContext()).apply {
            text = big
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, Typeface.BOLD)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val smallText = TextView(requireContext()).apply {
            text = small
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        guideLayer.addView(bigText)
        guideLayer.addView(smallText)

        guideLayer.post {
            val left = dp(18f)
            val top = dp(180f)

            bigText.x = left
            bigText.y = top

            smallText.x = left
            smallText.y = top + dp(44f)

            bigText.bringToFront()
            smallText.bringToFront()
        }
    }

    // SERVICE_MODAL_OPEN: 모달 열고, 닫히면(Tap X/추가하기) TODAY_TAB로 넘김
    private fun openAddServiceDialogForOnboarding() {
        val activity = requireActivity() as MainActivity

        AddServiceDialog(
            prefill = null,
            onSaved = { treatment, imageUri ->
                val photoPart: MultipartBody.Part? =
                    imageUri?.let { uriToMultipart(requireContext(), it, "photo") }

                shopId?.let {
                    shopServiceViewModel.createTreatment(
                        shopId = it,
                        request = treatment.toRequest(),
                        photo = photoPart
                    )
                }
            },

            onClosed = {
                activity.currentGuideStep = MainActivity.GuideStep.TODAY_TAB
                activity.hideGlobalOverlay()
                clearGuide()
                activity.refreshCurrentFragmentOverlay()
            }
        ).show(parentFragmentManager, "add_service_onboarding")
    }

    private fun clearGuide() {
        val guideLayer = getGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.GONE
    }

    private fun dp(v: Float): Float =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            v,
            resources.displayMetrics
        )

    /* ===================== Cleanup ===================== */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}