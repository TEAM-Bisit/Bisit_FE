package com.example.bisit.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.map.GeocodingResponse
import com.example.bisit.data.model.map.ShopMapItem
import com.example.bisit.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

    private var searchMarker: Marker? = null
    private val shopMarkers = mutableListOf<Marker>()

    private var nextCursor: Long? = null
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView
        // onCreate 등에서 mapView.onCreate() 호출이 필요한 경우 레이아웃에 따라 추가 (대부분 자동 처리됨)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapView.getMapAsync(this)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.customerCategoryFragment)
        }

        binding.cardSearch.setOnClickListener {
            binding.etSearch.requestFocus()
            Handler(Looper.getMainLooper()).postDelayed({
                // 안전한 Context 접근
                context?.let { ctx ->
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
                }
            }, 100)
        }

        binding.btnSearch.setOnClickListener { performSearch() }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "onMapReady 호출됨")
        this.naverMap = naverMap
        naverMap.locationSource = locationSource

        // 하드코딩된 좌표 대신 사용자의 현재 위치를 추적하도록 설정
        naverMap.locationOverlay.isVisible = true
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        naverMap.addOnLocationChangeListener { location ->
            Log.d(TAG, "현재 위치 수신: lat=${location.latitude}, lng=${location.longitude}")
        }

        naverMap.addOnCameraIdleListener {
            Log.d(TAG, "카메라 Idle → 영역 재조회")
            nextCursor = null
            fetchShops()
        }
    }

    private fun performSearch() {
        val query = binding.etSearch.text.toString()
        if (query.isNotBlank()) {
            searchAddress(query)
            clearShopMarkers()
            nextCursor = null
            fetchShopsByName(query)
        } else {
            Toast.makeText(context ?: return, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
        }

        context?.let { ctx ->
            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        }
    }

    private fun fetchShopsByName(name: String) {
        val currentContext = context ?: return // Fragment 분리 체크
        viewLifecycleOwner.lifecycleScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.getMapApi(currentContext)
                val response = api.searchShopsByName(name = name, cursor = nextCursor)

                if (response.isSuccessful && _binding != null) {
                    val data = response.body()?.data
                    data?.content?.forEach { addShopMarker(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchShopsByName Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchShops() {
        if (!::naverMap.isInitialized || isLoading) return
        val currentContext = context ?: return

        clearShopMarkers()

        val bounds = naverMap.contentBounds
        viewLifecycleOwner.lifecycleScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.getMapApi(currentContext)
                val response = api.searchShopsInBounds(
                    minLatitude = bounds.southLatitude,
                    maxLatitude = bounds.northLatitude,
                    minLongitude = bounds.westLongitude,
                    maxLongitude = bounds.eastLongitude,
                    cursor = nextCursor
                )

                if (response.isSuccessful && _binding != null) {
                    val shops = response.body()?.data?.content.orEmpty()
                    Log.d(TAG, "영역 검색 성공, 가게 수=${shops.size}")
                    shops.forEach { addShopMarker(it) }
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchShops Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun addShopMarker(shop: ShopMapItem) {
        if (_binding == null) return
        val marker = Marker().apply {
            position = LatLng(shop.latitude, shop.longitude)
            captionText = shop.shopName
            map = naverMap
        }
        shopMarkers.add(marker)
    }

    private fun clearShopMarkers() {
        shopMarkers.forEach { it.map = null }
        shopMarkers.clear()
    }

    private fun searchAddress(query: String) {
        RetrofitClient.geocodingApi.geocode(query)
            .enqueue(object : Callback<GeocodingResponse> {
                override fun onResponse(
                    call: Call<GeocodingResponse>,
                    response: Response<GeocodingResponse>
                ) {
                    if (_binding == null) return
                    val addr = response.body()?.addresses?.firstOrNull() ?: return
                    val lat = addr.y?.toDoubleOrNull()
                    val lng = addr.x?.toDoubleOrNull()

                    if (lat != null && lng != null) {
                        showMarker(LatLng(lat, lng))
                    }
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    context?.let {
                        Toast.makeText(it, "지오코딩 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun showMarker(latLng: LatLng) {
        if (!::naverMap.isInitialized) return
        searchMarker?.map = null
        searchMarker = Marker().apply {
            position = latLng
            map = naverMap
        }
        naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ) {
            if (locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            } else {
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}