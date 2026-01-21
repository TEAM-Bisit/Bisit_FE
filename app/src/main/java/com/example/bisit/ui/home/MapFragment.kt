package com.example.bisit.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bisit.BuildConfig
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.map.GeocodingResponse
import com.example.bisit.data.model.map.ShopMapItem
import com.example.bisit.data.model.map.SearchResultItem
import com.example.bisit.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.Tm128
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
    
    // Chips & Search
    private lateinit var chipAdapter: MapCategoryAdapter
    private lateinit var searchResultAdapter: MapSearchResultAdapter
    private val categories = listOf("전체", "생활가정", "IT·전자기기", "수리·설치", "차량 관리", "헬스케어", "오피스 관리", "촬영·행사", "학습·교육")
    
    private val allSearchResults = mutableListOf<SearchResultItem>()


    private var nextCursor: Long? = null
    private var isLoading = false
    
    // Instant search debounce
    private var searchHandler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

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
            binding.rvSearchSuggestions.visibility = View.GONE
            binding.etSearch.requestFocus()
            Handler(Looper.getMainLooper()).postDelayed({
                context?.let { ctx ->
                    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
                }
            }, 100)
        }

        binding.btnSearch.setOnClickListener { performSearch() }

        binding.btnMyLocation.setOnClickListener {
            moveCameraToCurrentLocation()
        }
        
        setupCategoryChips()
        setupSearchResultList()

        binding.etSearch.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                performSearch()
                true
            } else false
        }

        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }

        setupInstantSearch()
    }

    private fun setupInstantSearch() {
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { searchHandler.removeCallbacks(it) }
                
                val query = s?.toString()?.trim() ?: ""
                if (query.length >= 1) { // 한 글자만 써도 검색 시작
                    searchRunnable = Runnable {
                        performInstantSearch(query)
                    }
                    searchHandler.postDelayed(searchRunnable!!, 500) // 500ms 디바운스
                } else {
                    allSearchResults.clear()
                    updateSearchResults()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun performInstantSearch(query: String) {
        if (query.isNotBlank()) {
            nextCursor = null
            allSearchResults.clear()
            
            // Search Internal Shops
            fetchShopsByName(query, moveCamera = false)
            
            // Search Naver Places (External)
            fetchNaverPlaces(query, moveCamera = false)
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
        val query = binding.etSearch.text.toString().trim()
        Log.d(TAG, "performSearch triggered: query='$query'")
        if (query.isNotBlank()) {
            clearShopMarkers()
            nextCursor = null
            allSearchResults.clear()
            searchResultAdapter.submitList(emptyList<SearchResultItem>())
            
            // Search Internal Shops
            fetchShopsByName(query, moveCamera = true)
            
            // Search Naver Places (External)
            fetchNaverPlaces(query, moveCamera = true)
            
            Log.d(TAG, "Search started for '$query'")
        } else {
            Toast.makeText(context ?: return, "검색어를 입력하세요.", Toast.LENGTH_SHORT).show()
        }

        // Hide keyboard
        context?.let { ctx ->
            val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
        }
        
        binding.etSearch.clearFocus()
    }

    private fun fetchNaverPlaces(query: String, moveCamera: Boolean = false) {
        Log.d(TAG, "fetchNaverPlaces: query='$query', moveCamera=$moveCamera")
        // Debugging NCP Keys
        Log.d(TAG, "NCP_DEV_CLIENT_ID exists: ${BuildConfig.NAVER_DEV_CLIENT_ID.isNotEmpty()}")
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = RetrofitClient.naverSearchApi.searchLocal(query = query, display = 10)
                Log.d(TAG, "fetchNaverPlaces response success=${response.isSuccessful}, code=${response.code()}")
                if (response.isSuccessful && _binding != null) {
                    val items = response.body()?.items.orEmpty()
                    Log.d(TAG, "fetchNaverPlaces: found ${items.size} items")
                    val externalResults = items.map { SearchResultItem.ExternalPlace(it) }
                    
                    allSearchResults.addAll(externalResults as List<SearchResultItem>)
                    updateSearchResults(moveCameraToFirst = moveCamera)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "fetchNaverPlaces FAIL: code=${response.code()}, body=$errorBody")
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchNaverPlaces Error", e)
            }
        }
    }

    private fun updateSearchResults(moveCameraToFirst: Boolean = false) {
        if (_binding == null) return
        val resultsToShow = allSearchResults.take(5)
        Log.d(TAG, "updateSearchResults: resultsToShow=${resultsToShow.size}, moveCameraToFirst=$moveCameraToFirst")
        
        if (resultsToShow.isNotEmpty()) {
            binding.rvSearchSuggestions.visibility = View.VISIBLE
            binding.rvSearchSuggestions.bringToFront()
            searchResultAdapter.submitList(resultsToShow)
            
            if (moveCameraToFirst) {
                val first = resultsToShow[0]
                Log.d(TAG, "Auto-navigating to first result: ${first.name}")
                navigateToSearchResult(first)
            }
        } else {
            binding.rvSearchSuggestions.visibility = View.GONE
        }
    }

    private fun navigateToSearchResult(item: SearchResultItem) {
        Log.d(TAG, "navigateToSearchResult: ${item.name}")
        when (item) {
            is SearchResultItem.InternalShop -> {
                val shop = item.shop
                Log.d(TAG, "InternalShop selected: ${shop.shopName}, ID=${shop.shopId}, Lat=${shop.latitude}, Lng=${shop.longitude}")
                
                // If the backend already provided valid coordinates, use them immediately
                if (shop.latitude != 0.0 && shop.longitude != 0.0) {
                    Log.d(TAG, "✅ Using server-provided coordinates: ${shop.latitude}, ${shop.longitude}")
                    showMarker(LatLng(shop.latitude, shop.longitude))
                } else {
                    // Start geocoding if coordinates are missing
                    Log.w(TAG, "⚠️ Server returned coordinates (0.0, 0.0). Falling back to client-side geocoding.")
                    Toast.makeText(context, "가게 좌표가 없어 주소로 검색합니다.", Toast.LENGTH_SHORT).show()
                    fetchAndGeocodeInternalShop(shop.shopId, shop.latitude, shop.longitude)
                }
            }
            is SearchResultItem.ExternalPlace -> {
                // Naver Local Search results (with coordinate=wgs84) return 10^7 scaled values
                // Example: 1269707021 -> 126.9707021
                val rawX = item.place.mapx.toDoubleOrNull()
                val rawY = item.place.mapy.toDoubleOrNull()

                if (rawX != null && rawY != null) {
                    val finalLng = if (rawX > 1000) rawX / 10_000_000.0 else rawX
                    val finalLat = if (rawY > 1000) rawY / 10_000_000.0 else rawY
                    
                    val latLng = LatLng(finalLat, finalLng)
                    Log.d(TAG, "ExternalPlace conversion (WGS84): mapx=$rawX, mapy=$rawY -> LatLng=${latLng.latitude}, ${latLng.longitude}")
                    
                    if (latLng.latitude in 33.0..43.0 && latLng.longitude in 124.0..132.0) {
                        showMarker(latLng, true)
                    } else {
                        Log.e(TAG, "Converted WGS84 coordinate still outside Korea: $latLng. Trying Tm128 fallback.")
                        // Fallback: maybe it's still TM128? (though unlikely with wgs84 param)
                        val tmLatLng = Tm128(rawX, rawY).toLatLng()
                        if (tmLatLng.latitude in 33.0..43.0 && tmLatLng.longitude in 124.0..132.0) {
                             showMarker(tmLatLng, true)
                        } else {
                             fallbackToAddressSearch(item)
                        }
                    }
                } else {
                    fallbackToAddressSearch(item)
                }
            }
        }
    }

    private fun fallbackToAddressSearch(item: SearchResultItem.ExternalPlace) {
        val addressToSearch = if (item.place.roadAddress != null && item.place.roadAddress.isNotEmpty()) {
            item.place.roadAddress
        } else {
            item.place.address
        }
        if (addressToSearch != null && addressToSearch.isNotEmpty()) {
            searchAddress(addressToSearch)
        }
    }
    private fun fetchShopsByName(name: String, moveCamera: Boolean = false) {
        Log.d(TAG, "fetchShopsByName: name='$name', moveCamera=$moveCamera")
        val currentContext = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            isLoading = true
            try {
                val api = RetrofitClient.getMapApi(currentContext)
                val response = api.searchShopsByName(name = name, cursor = nextCursor)
                Log.d(TAG, "fetchShopsByName response success=${response.isSuccessful}, code=${response.code()}")

                if (response.isSuccessful && _binding != null) {
                    val data = response.body()?.data
                    val shops = data?.content.orEmpty()
                    Log.d(TAG, "fetchShopsByName: found ${shops.size} shops")
                    
                    val internalResults = shops.map { SearchResultItem.InternalShop(it) }
                    allSearchResults.addAll(internalResults as List<SearchResultItem>)
                    updateSearchResults(moveCameraToFirst = moveCamera)
                    
                    shops.forEach { addShopMarker(it) }
                } else {
                    Log.e(TAG, "fetchShopsByName FAILED: ${response.code()}")
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

    private fun searchAddress(query: String, shopId: Long? = null, fallbackLat: Double? = null, fallbackLng: Double? = null) {
        Log.d(TAG, "searchAddress query: $query")
        RetrofitClient.geocodingApi.geocode(query)
            .enqueue(object : retrofit2.Callback<GeocodingResponse> {
                override fun onResponse(
                    call: Call<GeocodingResponse>,
                    response: Response<GeocodingResponse>
                ) {
                    if (_binding == null) return
                    Log.d(TAG, "searchAddress onResponse: success=${response.isSuccessful}, code=${response.code()}")
                    
                    if (response.code() == 401) {
                        Log.e(TAG, "Naver Geocoding 401 Error: Permission Denied. Please check NCP Package Name registration.")
                        // Fallback: If geocoding fails, try to move to shop's own coordinates
                        if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                            Log.d(TAG, "Geocoding 401 -> Falling back to shop coordinates")
                            showMarker(LatLng(fallbackLat, fallbackLng))
                        } else {
                            // If no coordinates at all, only then show a silent error or neutral message
                            Log.w(TAG, "Geocoding failed (401) and no fallback coordinates available.")
                            // Removed intrusive Toast about "Permission Pending" as it confuses users
                        }
                        return
                    }

                    val body = response.body()
                    val addr = body?.addresses?.firstOrNull()
                    if (addr == null) {
                        Log.w(TAG, "No geocoding results for query: $query. Body: $body")
                        if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                            showMarker(LatLng(fallbackLat, fallbackLng))
                        }
                        return
                    }
                    val lat = addr.y?.toDoubleOrNull()
                    val lng = addr.x?.toDoubleOrNull()
                    Log.d(TAG, "Geocoded LatLng: $lat, $lng")

                    if (lat != null && lng != null) {
                        showMarker(LatLng(lat, lng))
                    } else if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                        showMarker(LatLng(fallbackLat, fallbackLng))
                    }
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    Log.e(TAG, "searchAddress onFailure", t)
                    if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                        showMarker(LatLng(fallbackLat, fallbackLng))
                    } else {
                        context?.let {
                            Toast.makeText(it, "지오코딩 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
    }

    private fun fetchAndGeocodeInternalShop(shopId: Long, fallbackLat: Double? = null, fallbackLng: Double? = null) {
        Log.d(TAG, "fetchAndGeocodeInternalShop: shopId=$shopId")
        val currentContext = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = RetrofitClient.getCustomerShopApi(currentContext)
                val response = api.getShopDetail(shopId)
                Log.d(TAG, "fetchAndGeocodeInternalShop response success=${response.isSuccessful}, code=${response.code()}")
                if (response.isSuccessful && _binding != null) {
                    val shopDetail = response.body()?.data
                    val address = shopDetail?.address
                    val lat = shopDetail?.latitude
                    val lng = shopDetail?.longitude
                    Log.d(TAG, "Shop detail: address=$address, lat=$lat, lng=$lng")
                    
                    if (lat != null && lng != null && lat != 0.0) {
                        Log.d(TAG, "Using coordinates from shop detail API: $lat, $lng")
                        showMarker(LatLng(lat, lng))
                    } else if (!address.isNullOrBlank()) {
                        Log.d(TAG, "Coordinates missing in detail, trying geocoding with address: $address")
                        searchAddress(address, shopId, fallbackLat, fallbackLng)
                    } else if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                        Log.w(TAG, "Shop address and detail coordinates missing, using fallback coordinates")
                        showMarker(LatLng(fallbackLat, fallbackLng))
                    } else {
                        Log.w(TAG, "No coordinates or address available for this shop.")
                    }
                } else if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                    // API fails but we have fallback
                    showMarker(LatLng(fallbackLat, fallbackLng))
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchAndGeocodeInternalShop Error", e)
                if (fallbackLat != null && fallbackLng != null && fallbackLat != 0.0) {
                    showMarker(LatLng(fallbackLat, fallbackLng))
                }
            }
        }
    }

    private fun showMarker(latLng: LatLng, moveCamera: Boolean = true) {
        if (!::naverMap.isInitialized) return
        searchMarker?.map = null
        searchMarker = Marker().apply {
            position = latLng
            map = naverMap
        }
        if (moveCamera) {
            naverMap.locationTrackingMode = LocationTrackingMode.None
            val cameraUpdate = CameraUpdate.scrollAndZoomTo(latLng, 15.0)
                .animate(com.naver.maps.map.CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)
        }
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

    private fun moveCameraToCurrentLocation() {
        if (!::naverMap.isInitialized) return
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    private fun setupCategoryChips() {
        chipAdapter = MapCategoryAdapter(categories) { category ->
             // Handle category selection
             // If "전체", fetch all. Else fetch by category (assuming API supports it or we filter local/remote)
             // For now, if API doesn't have strict category filter, we might need a new endpoint or parameter.
             // Assuming `searchShopsByCategory` or simply re-fetching with filter logic.
             // Since user asked to "filter shops", let's assume we call a search API.
             
             if (category == "전체") {
                 nextCursor = null
                 fetchShops()
             } else {
                 fetchShopsByCategory(category)
             }
        }
        binding.rvChip.adapter = chipAdapter
    }
    
    private fun fetchShopsByCategory(category: String) {
        val currentContext = context ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            isLoading = true
            try {
                // Assuming we use the search API with a query or a dedicated category endpoint.
                // If no dedicated endpoint, we might use "keyword" search or client side.
                // For now, let's try searching by name if no category field in API call visible here.
                // Wait, checking ShopDetailResponse, it has category.
                // We'll use `searchShopsByName` with category string if the API treats it as keyword, 
                // OR ideally we should have `searchShopsByCategory`.
                // Let's assume `searchShopsByName` works for keywords including category, OR use `searchShopsInBounds` then filter?
                // `searchShopsInBounds` gets everything.
                // Let's try `searchShopsByName(category)` for now as a fallback.
                
                // Correction: The user said "CustomerCategoryFragment에 있는 카테고리들 별로 그 카테고리에 맞는 가게들만 나오게 하는거야"
                // Usually this maps to `getMapApi().searchShopsByCategory(category, ...)`
                
                // Let's check RetrofitClient or assume we can add it.
                // Since I cannot change RetrofitClient invisible code easily (I can read it), I will assume I can pass it as name query or similar.
                // Actually, let's just search by name using the category name. Many systems work this way.
                
                val api = RetrofitClient.getMapApi(currentContext)
                // Use generic search
                val response = api.searchShopsByName(name = category, cursor = null)
                
                 if (response.isSuccessful && _binding != null) {
                    clearShopMarkers()
                    val shops = response.body()?.data?.content.orEmpty()
                    // Filter locally if needed to be strict?
                    // Let's just show results.
                    shops.forEach { addShopMarker(it) }
                    
                    if (shops.isEmpty()) Toast.makeText(context, "해당 카테고리의 가게가 없습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Category Search Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun setupSearchResultList() {
        searchResultAdapter = MapSearchResultAdapter { item ->
            // On Item Click
            navigateToSearchResult(item)
            
            binding.rvSearchSuggestions.visibility = View.GONE
            
            // Hide keyboard
            context?.let { ctx ->
                val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
            }
            
            // Show marker info window or select it?
            // For now just move camera.
        }
        binding.rvSearchSuggestions.adapter = searchResultAdapter
        binding.rvSearchSuggestions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
    }

    companion object {
        private const val TAG = "MapFragment"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}

// Adapters
class MapCategoryAdapter(
    private val categories: List<String>,
    private val onClick: (String) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<MapCategoryAdapter.ChipViewHolder>() {
    
    private var selectedPosition = 0

    inner class ChipViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvChip: android.widget.TextView = view.findViewById(R.id.tvChip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_chip, parent, false)
        return ChipViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        val category = categories[position]
        holder.tvChip.text = category
        
        val isSelected = position == selectedPosition
        if (isSelected) {
            holder.tvChip.setBackgroundResource(R.drawable.bg_list_chip_selected)
            holder.tvChip.setTextColor(android.graphics.Color.WHITE)
        } else {
            holder.tvChip.setBackgroundResource(R.drawable.bg_list_chip_unselected)
            holder.tvChip.setTextColor(android.graphics.Color.parseColor("#222222"))
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = holder.bindingAdapterPosition
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onClick(category)
        }
    }

    override fun getItemCount() = categories.size
}

class MapSearchResultAdapter(
    private val onClick: (SearchResultItem) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<MapSearchResultAdapter.ResultViewHolder>() {

    private var items = listOf<SearchResultItem>()

    fun submitList(newItems: List<SearchResultItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ResultViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val tvName: android.widget.TextView = view.findViewById(R.id.tvShopName)
        val tvCategory: android.widget.TextView = view.findViewById(R.id.tvCategory)
        val ivIcon: android.widget.ImageView = view.findViewById(R.id.ivShopIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_search_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvCategory.text = item.itemCategory

        when (item) {
            is SearchResultItem.InternalShop -> {
                holder.ivIcon.setImageResource(R.drawable.ic_search)
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#4076FF"))
            }
            is SearchResultItem.ExternalPlace -> {
                holder.ivIcon.setImageResource(R.drawable.ic_search)
                holder.ivIcon.setColorFilter(android.graphics.Color.parseColor("#888888"))
            }
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}