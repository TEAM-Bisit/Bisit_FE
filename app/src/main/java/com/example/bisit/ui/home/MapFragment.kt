package com.example.bisit.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.map.GeocodingResponse
import com.example.bisit.databinding.FragmentMapBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapView
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.NaverMap
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
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

    private val chipItems = listOf(
        "전체",
        "생활가정",
        "IT·전자기기",
        "수리·설치",
        "차량 관리"
    )

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
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapView.getMapAsync(this)

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.customerCategoryFragment)
        }

        binding.cardSearch.setOnClickListener {
            binding.etSearch.requestFocus()
            Handler(Looper.getMainLooper()).postDelayed({
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
            }, 100)
        }

        binding.btnSearch.setOnClickListener {
            val query = binding.etSearch.text.toString()
            if (query.isNotBlank()) {
                searchAddress(query)
            } else {
                Toast.makeText(requireContext(), "주소를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        val chipAdapter = MapChipAdapter(chipItems) { selected ->
            Toast.makeText(requireContext(), "$selected 선택됨", Toast.LENGTH_SHORT).show()
        }

        binding.rvChip.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvChip.adapter = chipAdapter
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow
    }

    private fun searchAddress(query: String) {
        RetrofitClient.geocodingApi.geocode(query)
            .enqueue(object : Callback<GeocodingResponse> {
                override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
                    if (response.isSuccessful) {
                        val addresses = response.body()?.addresses
                        if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            val lat = addr.y?.toDoubleOrNull()
                            val lng = addr.x?.toDoubleOrNull()

                            if (lat != null && lng != null) {
                                showMarker(
                                    LatLng(lat, lng),
                                    addr.roadAddress ?: addr.jibunAddress ?: "검색 결과"
                                )
                            } else {
                                Toast.makeText(requireContext(), "좌표 변환 실패", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "검색 결과 없음", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "지오코딩 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showMarker(latLng: LatLng, caption: String) {
        searchMarker?.map = null
        searchMarker = Marker().apply {
            position = latLng
            captionText = caption
            map = naverMap
        }
        naverMap.moveCamera(
            com.naver.maps.map.CameraUpdate.scrollTo(latLng)
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ) {
            if (!locationSource.isActivated) {
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
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}
