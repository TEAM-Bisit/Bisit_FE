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
import com.example.bisit.ui.shop.ShopFragment

class HomeListFragment : Fragment() {

    private var _binding: FragmentHomeListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyList = listOf(
            StoreItem(
                name = "장미헤어",
                category = "미용실 · 뷰티케어",
                rating = 4.9f,
                reviewCount = 12,
                isOpen = true,
                businessHours = "09:00 ~ 18:00",
                tags = listOf("컷트", "펌", "머리감기", "+3"),
                images = listOf(R.drawable.img_example, R.drawable.img_example),
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

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = HomeListAdapter(dummyList) { storeItem ->
            findNavController().navigate(R.id.action_homeListFragment_to_shopFragment)
        }

        binding.btnMap.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}