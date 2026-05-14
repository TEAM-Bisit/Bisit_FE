package kr.bisit.app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import kr.bisit.app.databinding.FragmentHomeListBinding
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.R
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLat: Double? = null
    private var userLng: Double? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            fetchCurrentLocation {
                resetPaginationAndLoad()
            }
        } else {
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

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

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermissionAndLoad()
    }

    private fun checkLocationPermissionAndLoad() {
        val fineLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation {
                resetPaginationAndLoad()
            }
        } else {
            requestPermissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    private fun fetchCurrentLocation(onComplete: () -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userLat = location.latitude
                    userLng = location.longitude
                    Log.d(TAG, "현재 위치 가져오기 성공: $userLat, $userLng")
                } else {
                    Log.w(TAG, "현재 위치를 가져올 수 없음 (null) -> 테스트를 위해 대구 좌표로 설정")
                    // 테스트를 위해 대구 좌표로 설정 (가게 주소가 대구임)
                    userLat = 35.8714
                    userLng = 128.6014
                }
                onComplete()
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "위치 권한 오류", e)
            onComplete()
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
        Log.d(TAG, "토큰 존재 여부: ${kr.bisit.app.data.api.TokenManager.getAccessToken(requireContext()) != null}")

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getShopApi(requireContext())
                
                val resp = api.getShopsByCategory(
                    category = currentCategory,
                    lat = userLat,
                    lng = userLng,
                    sortType = currentSortType,
                    cursor = cursor ?: 0L // 첫 페이지일 경우 0L 전달
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

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HomeListFragment onResume - Refreshing list")
        if (userLat != null && userLng != null) {
            resetPaginationAndLoad()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HomeListFragment 뷰 파괴됨")
        _binding = null
    }
}
