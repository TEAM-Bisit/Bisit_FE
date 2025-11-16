package com.example.bisit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerCategoryBinding

class CustomerCategoryFragment : Fragment() {

    private lateinit var binding: FragmentCustomerCategoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerCategoryBinding.inflate(inflater, container, false)

        setCategoryClickListeners()
        setMapButton()

        return binding.root
    }

    private fun setCategoryClickListeners() {

        val categoryViews = listOf(
            binding.categoryHome,
            binding.categoryIt,
            binding.categoryFix,
            binding.categoryCar,
            binding.categoryHealth,
            binding.categoryOffice,
            binding.categoryCamera,
            binding.categoryEdu
        )

        val categoryNames = listOf(
            "생활가정", "IT·전자기기", "수리·설치", "차량 관리",
            "헬스케어", "오피스 관리", "촬영·행사", "학습·교육"
        )

        categoryViews.forEachIndexed { index, view ->
            view.setOnClickListener {

                val bundle = Bundle().apply {
                    putString("category", categoryNames[index])
                }

                findNavController().navigate(
                    R.id.homeListFragment,
                    bundle
                )
            }
        }
    }

    private fun setMapButton() {
        binding.btnMap.setOnClickListener {
            findNavController().navigate(R.id.mapFragment)
        }
    }
}
