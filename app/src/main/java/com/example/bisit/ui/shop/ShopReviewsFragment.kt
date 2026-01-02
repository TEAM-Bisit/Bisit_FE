package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentShopReviewsBinding
import com.example.bisit.ui.shop.adapter.ReviewAdapter
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import kotlinx.coroutines.launch

class ShopReviewsFragment : Fragment() {

    private var _binding: FragmentShopReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReviewAdapter

    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
    }

    private val shopReviewsViewModel: ShopReviewsViewModel by viewModels()

    /** 삭제 대상 리뷰 ID */
    private var selectedReviewId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeShopId()
        observeViewModel()
        observeBottomSheetResult()
    }

    /* ===================== RecyclerView ===================== */

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(
            onMoreClick = { review ->
                selectedReviewId = review.reviewId
                BottomActionSheet
                    .newInstance(BottomActionSheet.TYPE_REVIEW)
                    .show(parentFragmentManager, "bottom_action_sheet")
            }
        )

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter
    }

    /* ===================== shopId 연결 ===================== */

    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId?.let {
                    shopReviewsViewModel.setShopId(it)
                    shopReviewsViewModel.fetchReviews()
                }
            }
        }
    }

    /* ===================== ViewModel 관찰 ===================== */

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            shopReviewsViewModel.reviews.collect { reviews ->
                adapter.submitList(reviews)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            shopReviewsViewModel.errorMessage.collect { message ->
                // TODO: Snackbar 또는 Toast 처리
            }
        }
    }

    /* ===================== BottomSheet 결과 ===================== */

    private fun observeBottomSheetResult() {
        parentFragmentManager.setFragmentResultListener(
            BottomActionSheet.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            when (bundle.getString(BottomActionSheet.RESULT_ACTION)) {
                BottomActionSheet.ACTION_DELETE -> {
                    showDeleteConfirmDialog()
                }
            }
        }
    }

    /* ===================== 삭제 확인 다이얼로그 ===================== */

    private fun showDeleteConfirmDialog() {
        val reviewId = selectedReviewId ?: return

        ConfirmDialog(
            message = "해당 리뷰를 삭제하시겠습니까?",
            onConfirm = {
                shopReviewsViewModel.deleteReview(reviewId)
            }
        ).show(parentFragmentManager, "confirm_delete")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
