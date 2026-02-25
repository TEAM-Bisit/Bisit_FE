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

        binding.fabAdd.setOnClickListener {
            openAddServiceDialog()
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
                    shopServiceViewModel.loadTreatments(
                        shopId = it,
                        isFirst = true
                    )
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

                BottomActionSheet.ACTION_DELETE -> {
                    showDeleteConfirm(item)
                }

                BottomActionSheet.ACTION_EDIT -> {
                    openEditServiceDialog(item)
                }
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

    /* ===================== 서비스 추가 ===================== */

    private fun openAddServiceDialog() {
        AddServiceDialog { treatment, imageUri ->

            val photoPart: MultipartBody.Part? =
                imageUri?.let {
                    uriToMultipart(requireContext(), it, "photo")
                }

            shopId?.let {
                shopServiceViewModel.createTreatment(
                    shopId = it,
                    request = treatment.toRequest(),
                    photo = photoPart
                )
            }
        }.show(parentFragmentManager, "add_service")
    }

    /* ===================== 서비스 수정 ===================== */

    private fun openEditServiceDialog(item: TreatmentResponse) {
        AddServiceDialog(prefill = item) { updated, imageUri ->

            val photoPart: MultipartBody.Part? =
                imageUri?.let {
                    uriToMultipart(requireContext(), it, "photo")
                }

            shopId?.let {
                shopServiceViewModel.updateTreatment(
                    treatmentId = updated.treatmentId,
                    shopId = it,
                    request = updated.toRequest(),
                    photo = photoPart
                )
            }
        }.show(parentFragmentManager, "edit_service")
    }


    fun refreshOnboarding() {

        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide()
            return
        }

        binding.fabAdd.post {

            if (activity.currentGuideStep ==
                MainActivity.GuideStep.SERVICE_SCREEN
            ) {

                activity.showGlobalOverlay(
                    targetView = binding.fabAdd,
                    shape = HighlightOverlayView.HighlightShape.CIRCLE,
                    radiusDp = 40f
                )

                showGuideTextAboveFab(binding.fabAdd)
            }
        }
    }

    private fun showGuideTextAboveFab(targetView: View) {

        clearGuide()
        binding.guideLayer.visibility = View.VISIBLE

        val guideText = TextView(requireContext()).apply {
            text = "이곳을 누르세요!"
            setTextColor(0xFFFFFFFF.toInt())
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }

        binding.guideLayer.addView(guideText)

        guideText.post {

            val rect = Rect()
            targetView.getGlobalVisibleRect(rect)

            val layerLocation = IntArray(2)
            binding.guideLayer.getLocationOnScreen(layerLocation)

            val localRight = rect.right - layerLocation[0]
            val localTop = rect.top - layerLocation[1]

            val margin8dp = 8 * resources.displayMetrics.density
            val margin18dp = 18 * resources.displayMetrics.density

            guideText.x = localRight - guideText.width - margin18dp
            guideText.y = localTop - guideText.height - margin8dp
        }
    }

    private fun clearGuide() {
        binding.guideLayer.removeAllViews()
        binding.guideLayer.visibility = View.GONE
    }

    /* ===================== Cleanup ===================== */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
