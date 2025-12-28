package com.example.bisit.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.bisit.databinding.FragmentHomeListBinding
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.R
import kotlinx.coroutines.launch

class HomeListFragment : Fragment() {

    private var _binding: FragmentHomeListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeListAdapter

    private var currentSortType: String = "DISTANCE"
    private var currentCategory: String = "LIVING"

    private val TAG = "HomeListFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "===== HomeListFragment START =====")

        val categoryName = arguments?.getString("category") ?: "생활가정"
        Log.d(TAG, "받은 카테고리 인자 = $categoryName")

        currentCategory = mapCategoryToEnum(categoryName)
        Log.d(TAG, "매핑된 category enum = $currentCategory")

        binding.tvTitle.text = "$categoryName 보기"

        setupFilterChips()
        setupRecyclerView()

        loadShopList()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupFilterChips() {
        val filterItems = listOf("가까운 순", "별점 높은 순", "리뷰 많은 순")

        val filterAdapter = HomeFilterChipAdapter(filterItems) { selected ->
            Log.d(TAG, "필터 선택됨: $selected")

            currentSortType = when (selected) {
                "가까운 순" -> "DISTANCE"
                "별점 높은 순" -> "RATING"
                "리뷰 많은 순" -> "REVIEW_COUNT"
                else -> "DISTANCE"
            }

            Log.d(TAG, "변경된 정렬 타입 = $currentSortType")
            loadShopList()
        }

        binding.rvFilters.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.adapter = filterAdapter
    }

    private fun setupRecyclerView() {
        adapter = HomeListAdapter(emptyList()) {
            Log.d(TAG, "가게 아이템 클릭됨 → 상세 페이지 이동")
            findNavController().navigate(R.id.action_homeListFragment_to_customerShopFragment)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun loadShopList() {
        Log.d(TAG, "===== loadShopList() 호출됨 =====")
        Log.d(TAG, "요청 파라미터 → category=$currentCategory, sort=$currentSortType")

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getShopApi(requireContext())
                Log.d(TAG, "ShopApi 객체 생성 성공")

                val resp = api.getShopsByCategory(
                    category = currentCategory,
                    lat = 37.5665,     // TODO: 실제 위치 넣기
                    lng = 126.9780,
                    sortType = currentSortType
                )

                Log.d(TAG, "API 응답 성공")
                Log.d(TAG, "응답 전체 = $resp")

                Log.d(TAG, "응답 리스트 사이즈 = ${resp.data.content.size}")
                adapter.updateData(resp.data.content)

            } catch (e: Exception) {
                Log.e(TAG, "API 요청 중 오류 발생", e)
                e.printStackTrace()
            }
        }
    }

    private fun mapCategoryToEnum(name: String): String {
        Log.d(TAG, "mapCategoryToEnum() 호출 name=$name")
        return when (name) {
            "생활가정" -> "LIVING"
            "IT·전자기기" -> "IT_ELECTRONICS"
            "수리·설치" -> "REPAIR_INSTALLATION"
            "차량 관리" -> "VEHICLE_MANAGEMENT"
            "헬스케어" -> "HEALTHCARE"
            "오피스 관리" -> "OFFICE_MANAGEMENT"
            "촬영·행사" -> "PHOTO_EVENT"
            "학습·교육" -> "EDUCATION"
            else -> "LIVING"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HomeListFragment 뷰 파괴됨")
        _binding = null
    }
}
