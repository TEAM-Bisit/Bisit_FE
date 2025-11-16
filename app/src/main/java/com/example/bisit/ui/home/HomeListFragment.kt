package com.example.bisit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.bisit.databinding.FragmentHomeListBinding
import com.example.bisit.data.model.store.StoreItem
import com.example.bisit.R

class HomeListFragment : Fragment() {

    private var _binding: FragmentHomeListBinding? = null
    private val binding get() = _binding!!

    private val filterItems = listOf(
        "가까운 순",
        "별점 높은 순",
        "리뷰 많은 순"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString("category") ?: "전체"

        binding.tvTitle.text = "$categoryName 보기"

        val filterAdapter = HomeFilterChipAdapter(filterItems) { selectedFilter ->

        }

        binding.rvFilters.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        binding.rvFilters.adapter = filterAdapter

        val dummyList = getDummyList(categoryName)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = HomeListAdapter(dummyList) {
            findNavController().navigate(R.id.action_homeListFragment_to_customerShopFragment)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }


    private fun getDummyList(category: String?): List<StoreItem> {
        val all = listOf(
            StoreItem(
                name = "장미헤어",
                category = "미용실 · 뷰티케어",
                rating = 4.9f,
                reviewCount = 12,
                isOpen = true,
                businessHours = "09:00 ~ 18:00",
                tags = listOf("컷트", "펌", "머리감기", "+3"),
                images = listOf(R.drawable.img_example),
                hasVisitService = true
            ),
            StoreItem(
                name = "Hand & Toe",
                category = "네일 · 뷰티케어",
                rating = 5.0f,
                reviewCount = 14,
                isOpen = false,
                businessHours = "09:00 ~ 18:00",
                tags = listOf("네일", "핸드케어", "큐티클"),
                images = listOf(R.drawable.img_example),
                hasVisitService = false
            )
        )
        return all
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
