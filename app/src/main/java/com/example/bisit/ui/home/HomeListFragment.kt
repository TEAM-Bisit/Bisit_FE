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
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HomeListFragment : Fragment() {

    private var _binding: FragmentHomeListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeListAdapter

    private var currentSortType: String = "DISTANCE"
    private var currentCategory: String = "LIVING"
    
    private var nextCursor: Long? = null
    private var hasNext: Boolean = false
    private var isLoading: Boolean = false

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
        setupInfiniteScroll()

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
            resetPaginationAndLoad()
        }

        binding.rvFilters.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.adapter = filterAdapter
    }

    private fun setupRecyclerView() {
        adapter = HomeListAdapter(emptyList()) {
            Log.d(TAG, "가게 아이템 클릭됨 → 상세 페이지 이동: ${it.shopId}")
            val bundle = Bundle().apply {
                putLong("shopId", it.shopId)
            }
            findNavController().navigate(R.id.action_homeListFragment_to_customerShopFragment, bundle)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun setupInfiniteScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount

                if (!isLoading && hasNext && lastVisibleItemPosition + 5 >= totalItemCount) {
                    Log.d(TAG, "Infinite Scroll triggered: loading with nextCursor=$nextCursor")
                    loadShopList(nextCursor)
                }
            }
        })
    }

    private fun resetPaginationAndLoad() {
        nextCursor = null
        hasNext = false
        adapter.updateData(emptyList()) // Clear list
        loadShopList(null)
    }

    private fun loadShopList(cursor: Long? = null) {
        if (isLoading) return
        isLoading = true
        
        Log.d(TAG, "===== loadShopList() 호출됨 (cursor=$cursor) =====")
        Log.d(TAG, "요청 파라미터 → category=$currentCategory, sort=$currentSortType")

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getShopApi(requireContext())
                
                val resp = api.getShopsByCategory(
                    category = currentCategory,
                    lat = 37.5665,     // TODO: 실제 위치 넣기
                    lng = 126.9780,
                    sortType = currentSortType,
                    cursor = cursor
                )

                Log.d(TAG, "API 응답 성공: size=${resp.data.content.size}, hasNext=${resp.data.hasNext}, nextCursor=${resp.data.nextCursor}")
                
                if (cursor == null) {
                    adapter.updateData(resp.data.content)
                } else {
                    adapter.addData(resp.data.content)
                }

                nextCursor = resp.data.nextCursor
                hasNext = resp.data.hasNext

            } catch (e: Exception) {
                Log.e(TAG, "API 요청 중 오류 발생", e)
                Toast.makeText(requireContext(), "가게 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
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
