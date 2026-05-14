package kr.bisit.app.ui.home

import android.Manifest
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
import kr.bisit.app.R
import kr.bisit.app.databinding.FragmentCustomerCategoryBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
            // lastLocation 대신 getCurrentLocation을 사용하여 더 신선한 위치 정보를 가져옴
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (!isAdded) return@addOnSuccessListener
                    
                    if (location != null) {
                        val geocoder = Geocoder(requireContext(), Locale.KOREA)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                if (addresses.isNotEmpty() && isAdded) {
                                    updateLocationText(addresses[0])
                                }
                            }
                        } else {
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            if (!addresses.isNullOrEmpty()) {
                                updateLocationText(addresses[0])
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

    private fun updateLocationText(address: android.location.Address) {
        activity?.runOnUiThread {
            // 주소에서 시/도, 구/군 정도만 추출하여 깔끔하게 표시
            // 예: "서울특별시 강남구"
            val state = address.adminArea ?: "" // 서울특별시
            val city = address.locality ?: ""    // 
            val subLocality = address.subLocality ?: "" // 강남구
            val thoroughfare = address.thoroughfare ?: "" // 역삼동
            
            val sb = StringBuilder()
            if (state.isNotEmpty()) sb.append(state).append(" ")
            if (city.isNotEmpty() && city != state) sb.append(city).append(" ")
            if (subLocality.isNotEmpty()) sb.append(subLocality).append(" ")
            
            var simpleAddress = sb.toString().trim()
            
            // 만약StringBuilder 결과가 비어있다면 전체 주소에서 앞부분만 사용
            if (simpleAddress.isEmpty()) {
                val fullAddress = address.getAddressLine(0).replace("대한민국 ", "")
                val parts = fullAddress.split(" ")
                simpleAddress = if (parts.size >= 2) "${parts[0]} ${parts[1]}" else fullAddress
            }

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
