package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentShopReviewsBinding
import com.example.bisit.ui.shop.adapter.ReviewAdapter
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import com.example.bisit.ui.shop.model.Review
import com.example.bisit.ui.todayReserv.dialog.SortOptionDialog
import java.text.SimpleDateFormat
import java.util.*

class ShopReviewsFragment : Fragment() {

    private var _binding: FragmentShopReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReviewAdapter

    private val data = mutableListOf(
        Review(1, "2025.09.12", "염컬", "김*영", 5, "마음에 들어요!"),
        Review(2, "2025.09.10", "셋팅펌", "박*민", 4, "친절했습니다.")
    )

    // 현재 정렬 상태 (true = 최신순)
    private var isRecentSort = true

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

        adapter = ReviewAdapter(onMoreClick = { review ->
            // BottomActionSheet 표시
            BottomActionSheet().show(parentFragmentManager, "actions")

            // 결과 리스너 등록
            parentFragmentManager.setFragmentResultListener(
                BottomActionSheet.REQUEST_KEY,
                viewLifecycleOwner
            ) { _, bundle ->
                when (bundle.getString(BottomActionSheet.RESULT_ACTION)) {
                    BottomActionSheet.ACTION_DELETE -> {
                        ConfirmDialog(
                            message = "삭제하시겠어요?",
                            okText = "삭제하기",
                            onOk = {
                                data.removeAll { it.id == review.id }
                                sortReviews(isRecentSort)
                            }
                        ).show(parentFragmentManager, "confirm")
                    }

                    BottomActionSheet.ACTION_EDIT -> {
                        // TODO: 리뷰 수정 로직
                        // AddReviewDialog(prefill = review) { updated -> ... }
                    }
                }
            }
        })

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter
        adapter.submitList(data.toList())

        // 정렬 옵션 다이얼로그
        binding.tvSortLabel.setOnClickListener {
            SortOptionDialog(isRecentSort) { selectedRecent ->
                isRecentSort = selectedRecent
                sortReviews(selectedRecent)
            }.show(parentFragmentManager, "sort_option")
        }
    }

    private fun sortReviews(isRecent: Boolean) {
        val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
        val sorted = if (isRecent) {
            data.sortedByDescending { sdf.parse(it.date) }
        } else {
            data.sortedBy { sdf.parse(it.date) }
        }
        adapter.submitList(sorted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
