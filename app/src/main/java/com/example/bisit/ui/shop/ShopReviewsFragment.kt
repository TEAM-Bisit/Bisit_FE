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
import com.example.bisit.ui.todayReserv.dialog.SortOptionDialog
import java.text.SimpleDateFormat
import java.util.*

import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.review.ReviewDetailItem
import com.example.bisit.data.model.review.ReviewListResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShopReviewsFragment : Fragment() {

    private var _binding: FragmentShopReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReviewAdapter

    // No local data needed, will fetch from API
    
    private var currentSort: String = "recent"

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
            // Logic for more click, e.g., report?
            // Shop reviews usually don't have delete/edit for the viewer unless it's their own or admin.
            // Keeping existing structure but maybe disable actions if not owner.
        })

        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = adapter

        // Fetch Data
        fetchReviews()

        // 정렬 옵션 다이얼로그
        binding.tvSortLabel.setOnClickListener {
            SortOptionDialog(currentSort) { selectedSort ->
                currentSort = selectedSort
                binding.tvSortLabel.text =
                    if (selectedSort == "recent") "최근 순으로" else "오래된 순으로"
                
                // Re-fetch or sorting logic if API supports sort
                // API provided doesn't have sort param, assuming backend handles "recent" by default?
                // Or client side sort? client side sort is hard with pagination.
                // For now, re-fetch.
                fetchReviews() 
            }.show(parentFragmentManager, "sort_option")
        }
    }

    private fun fetchReviews() {
        val shopId = arguments?.getLong("shopId") ?: 1L 
        
        RetrofitClient.getReviewApi(requireContext()).getShopReviews(shopId, 0, 10)
            .enqueue(object : Callback<ReviewListResponse> {
                override fun onResponse(
                    call: Call<ReviewListResponse>,
                    response: Response<ReviewListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val reviewPage = response.body()?.data?.reviews
                        val items = reviewPage?.content ?: emptyList()
                        
                        // Client side sort if needed or provided by API
                        // The user said "리뷰는 최신순으로 정렬됩니다" (reviews are sorted by latest) by default.
                        adapter.submitList(items)
                    }
                }

                override fun onFailure(call: Call<ReviewListResponse>, t: Throwable) {
                    // Handle error
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
