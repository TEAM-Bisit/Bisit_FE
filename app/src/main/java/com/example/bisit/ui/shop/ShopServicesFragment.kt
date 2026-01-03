package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

    /* ===================== ViewModels ===================== */

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
                    photo = photoPart // null이면 기존 이미지 유지
                )
            }
        }.show(parentFragmentManager, "edit_service")
    }

    /* ===================== Cleanup ===================== */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
