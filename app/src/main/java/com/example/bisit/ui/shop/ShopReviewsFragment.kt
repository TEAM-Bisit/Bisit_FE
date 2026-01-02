package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    // shopId 제공 VM
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels()

    // 리뷰 관리 VM
    private lateinit var shopReviewsViewModel: ShopReviewsViewModel

    // 삭제 대상 리뷰 ID
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

        shopReviewsViewModel = ShopReviewsViewModel(requireContext())

        setupRecyclerView()
        observeShopId()
        observeViewModel()
        observeBottomSheetResult()
    }

    /* ===================== RecyclerView ===================== */

    private fun setupRecyclerView() {
        adapter = ReviewAdapter(
            onMoreClick = { review ->
                // 삼 점 클릭 → 삭제 BottomSheet
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
        lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId?.let {
                    shopReviewsViewModel.initShop(it)
                }
            }
        }
    }

    /* ===================== ViewModel 관찰 ===================== */
    private fun observeViewModel() {
        lifecycleScope.launch {
            shopReviewsViewModel.reviews.collect { reviews ->
                adapter.submitList(reviews)
            }
        }

        lifecycleScope.launch {
            shopReviewsViewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            shopReviewsViewModel.errorMessage.collect { message ->
                // TODO: Snackbar / Toast 처리 가능
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
            title = "리뷰 삭제",
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
