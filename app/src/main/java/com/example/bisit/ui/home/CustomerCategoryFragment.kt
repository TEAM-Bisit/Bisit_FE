package com.example.bisit.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerCategoryBinding
import com.google.android.gms.location.LocationServices
import java.util.Locale

class CustomerCategoryFragment : Fragment() {

    private lateinit var binding: FragmentCustomerCategoryBinding

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                getCurrentLocation()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                getCurrentLocation()
            }
            else -> {
                binding.tvLocation.text = "위치 권한이 필요합니다"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomerCategoryBinding.inflate(inflater, container, false)

        setCategoryClickListeners()
        setMapButton()
        checkLocationPermission()

        return binding.root
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (!isAdded) return@addOnSuccessListener
                
                if (location != null) {
                    val geocoder = Geocoder(requireContext(), Locale.KOREA)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            if (addresses.isNotEmpty() && isAdded) {
                                updateLocationText(addresses[0].getAddressLine(0))
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {
                            updateLocationText(addresses[0].getAddressLine(0))
                        }
                    }
                } else {
                    binding.tvLocation.text = "위치 정보를 가져올 수 없습니다"
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateLocationText(address: String) {
        activity?.runOnUiThread {
            // "대한민국" 같은 국가명은 제거하고 싶다면 아래와 같이 처리 가능
            val simpleAddress = address.replace("대한민국 ", "")
            binding.tvLocation.text = simpleAddress
        }
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
