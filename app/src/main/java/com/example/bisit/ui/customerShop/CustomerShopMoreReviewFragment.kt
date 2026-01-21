package com.example.bisit.ui.customerShop

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.data.model.shop.ReviewItem
import com.example.bisit.data.repository.customerShop.CustomerShopRepository
import com.example.bisit.databinding.FragmentCustomerShopMoreReviewBinding

class CustomerShopMoreReviewFragment : Fragment() {

    private var _binding: FragmentCustomerShopMoreReviewBinding? = null
    private val binding get() = _binding!!

    // Reuse existing ViewModel to share data (or creating new instance if scoped to fragment)
    // Here we create a new instance as it's a new fragment, but using same repository logic.
    private val viewModel: CustomerShopViewModel by viewModels {
        CustomerShopViewModelFactory(CustomerShopRepository(requireContext()))
    }

    private lateinit var adapter: CustomerShopMoreReviewAdapter
    private var currentSortMode = SortMode.HIGH_RATING
    private var shopId: Long = -1L

    enum class SortMode {
        HIGH_RATING, LOW_RATING, RECENT
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerShopMoreReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shopId = arguments?.getLong("shopId") ?: -1L
        if (shopId == -1L) {
             Toast.makeText(requireContext(), "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
             findNavController().popBackStack()
             return
        }

        setupUI()
        observeData()

        viewModel.loadShopReviews(requireContext(), shopId)
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnHome.setOnClickListener { findNavController().navigate(R.id.customerCategoryFragment) }

        adapter = CustomerShopMoreReviewAdapter(emptyList())
        binding.rvMoreReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMoreReviews.adapter = adapter

        binding.sortHighRating.setOnClickListener { setSortMode(SortMode.HIGH_RATING) }
        binding.sortLowRating.setOnClickListener { setSortMode(SortMode.LOW_RATING) }
        binding.sortRecent.setOnClickListener { setSortMode(SortMode.RECENT) }
    }

    private fun setSortMode(mode: SortMode) {
        currentSortMode = mode
        updateSortChips()
        applySort()
    }

    private fun updateSortChips() {
        val context = requireContext()
        val selectedBg = R.drawable.bg_sort_chip_selected
        val unselectedBg = R.drawable.bg_sort_chip_unselected
        val selectedTxt = Color.WHITE
        val unselectedTxt = Color.parseColor("#555555")

        fun updateChip(view: TextView, isSelected: Boolean) {
            view.setBackgroundResource(if (isSelected) selectedBg else unselectedBg)
            view.setTextColor(if (isSelected) selectedTxt else unselectedTxt)
        }

        updateChip(binding.sortHighRating, currentSortMode == SortMode.HIGH_RATING)
        updateChip(binding.sortLowRating, currentSortMode == SortMode.LOW_RATING)
        updateChip(binding.sortRecent, currentSortMode == SortMode.RECENT)
    }

    private fun observeData() {
        viewModel.reviewsData.observe(viewLifecycleOwner) { reviews ->
            applySort(reviews)
        }
    }

    private fun applySort(list: List<ReviewItem>? = viewModel.reviewsData.value) {
        if (list == null) return
        val sorted = when (currentSortMode) {
            SortMode.HIGH_RATING -> list.sortedByDescending { it.rating }
            SortMode.LOW_RATING -> list.sortedBy { it.rating }
            SortMode.RECENT -> list.sortedByDescending { it.date } // Assuming date format allows string sort or parse it
        }
        adapter.updateData(sorted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
