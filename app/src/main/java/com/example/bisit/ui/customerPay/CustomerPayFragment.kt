package com.example.bisit.ui.customerPay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.reservation.ReservationRequest
import com.example.bisit.databinding.FragmentCustomerPayBinding
import com.example.bisit.data.model.coupon.ApplicableCoupon
import kotlinx.coroutines.launch

class CustomerPayFragment : Fragment() {

    private var _binding: FragmentCustomerPayBinding? = null
    private val binding get() = _binding!!

    // Reservation data from previous screen
    private var shopId: Long = -1L
    private var staffId: Long = -1L
    private var treatmentId: Long = -1L
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var visitType: String = ""
    private var totalPrice: Int = 0
    private var serviceName: String = ""
    private var staffName: String = ""
    private var shopName: String = ""

    // State variables to preserve form data
    private var savedName: String = ""
    private var savedPhone: String = ""
    private var savedAddress: String = ""
    private var savedDetailAddress: String = ""
    private var savedCheckboxState: Boolean = false
    
    // Selected Coupon
    private var selectedCoupon: ApplicableCoupon? = null

    private val addressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val addr = result.data?.getStringExtra("selectedAddress") ?: ""
            binding.tvSelectedAddress.text = addr
            savedAddress = addr
            binding.etDetailAddress.requestFocus()
            validateForm()
        }
    }

    private val paymentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val paymentSuccess = result.data?.getBooleanExtra(TossPayActivity.RESULT_PAYMENT_SUCCESS, false) ?: false
            
            if (paymentSuccess) {
                // Payment successful (server confirmed it)
                Toast.makeText(
                    requireContext(),
                    "결제 및 예약이 완료되었습니다!",
                    Toast.LENGTH_LONG
                ).show()
                
                // Navigate back
                findNavController().popBackStack()
            }
        } else {
            Toast.makeText(
                requireContext(),
                "결제가 취소되었습니다. 예약이 대기 상태로 남을 수 있습니다.",
                Toast.LENGTH_SHORT
            ).show()
            // Reset button to allow retry
            binding.btnPay.isEnabled = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerPayBinding.inflate(inflater, container, false)

        // Get reservation data from arguments
        arguments?.let {
            shopId = it.getLong("shopId", -1L)
            staffId = it.getLong("staffId", -1L)
            treatmentId = it.getLong("treatmentId", -1L)
            selectedDate = it.getString("selectedDate", "")
            selectedTime = it.getString("selectedTime", "")
            visitType = it.getString("visitType", "")
            totalPrice = it.getInt("totalPrice", 0)
            serviceName = it.getString("serviceName", "")
            staffName = it.getString("staffName", "")
            shopName = it.getString("shopName", "")
        }

        // Restore saved state
        restoreSavedState(savedInstanceState)

        setupReservationInfo()
        setupBackButton()
        setupCouponClick()
        setupCheckBox()
        setupExpandableLayouts()
        setupPayButton()
        setupAddressSearch()
        setupTextWatchers()

        // Check for returned couponResult
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ApplicableCoupon>("selectedCoupon")
            ?.observe(viewLifecycleOwner) { coupon ->
                selectedCoupon = coupon
                updatePriceWithCoupon()
            }

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current state
        outState.putString("name", binding.etName.text.toString())
        outState.putString("phone", binding.etPhone.text.toString())
        outState.putString("address", binding.tvSelectedAddress.text.toString())
        outState.putString("detailAddress", binding.etDetailAddress.text.toString())
        outState.putBoolean("checkbox", binding.cbAgree.isChecked)
    }

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        // Restore from savedInstanceState first, then from saved variables
        if (savedInstanceState != null) {
            savedName = savedInstanceState.getString("name", "")
            savedPhone = savedInstanceState.getString("phone", "")
            savedAddress = savedInstanceState.getString("address", "주소를 선택해주세요")
            savedDetailAddress = savedInstanceState.getString("detailAddress", "")
            savedCheckboxState = savedInstanceState.getBoolean("checkbox", false)
        }
        
        // Apply saved values to views
        binding.etName.setText(savedName)
        binding.etPhone.setText(savedPhone)
        if (savedAddress.isNotEmpty() && savedAddress != "주소를 선택해주세요") {
            binding.tvSelectedAddress.text = savedAddress
        }
        binding.etDetailAddress.setText(savedDetailAddress)
        binding.cbAgree.isChecked = savedCheckboxState
        
        // Validate form with restored values
        validateForm()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            // Save current state before going back
            savedName = binding.etName.text.toString()
            savedPhone = binding.etPhone.text.toString()
            savedAddress = binding.tvSelectedAddress.text.toString()
            savedDetailAddress = binding.etDetailAddress.text.toString()
            savedCheckboxState = binding.cbAgree.isChecked
            
            // popBackStack()을 사용하여 이전 Fragment로 돌아가면 상태가 유지됨
            findNavController().popBackStack()
        }
    }

    private fun setupCouponClick() {
        binding.layoutCoupon.setOnClickListener {
             val bundle = Bundle().apply {
                putInt("treatmentPrice", totalPrice)
             }
            findNavController().navigate(
                R.id.action_customerPayFragment_to_customerPayCouponFragment,
                bundle
            )
        }

        // Initialize pricing views
        binding.tvProductPrice.text = "${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalPrice)}원"
        binding.tvTotalPrice.text = "${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalPrice)}원"
    }

    private fun updatePriceWithCoupon() {
        if (selectedCoupon != null) {
            val discount = selectedCoupon?.expectedDiscount ?: 0
            val finalPrice = totalPrice - discount
             binding.tvDiscount.text = "-${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(discount)}원"
             binding.tvTotalPrice.text = "${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(finalPrice)}원"
             
             // Update coupon name text and color
             binding.tvCouponSelect.text = selectedCoupon?.name
             binding.tvCouponSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_4076FF))
             
             // Hide coupon count if coupon is selected (optional, adjusting based on typical UI pattern)
             binding.tvCouponSelectCount.visibility = View.GONE
        } else {
             // Reset UI
             binding.tvDiscount.text = "0원"
             binding.tvTotalPrice.text = "${java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(totalPrice)}원"
             binding.tvCouponSelect.text = "적용 가능한 쿠폰"
             binding.tvCouponSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_515965)) // Assuming a gray color exists or use hex
             binding.tvCouponSelectCount.visibility = View.VISIBLE
        }
    }

    private fun setupCheckBox() {
        binding.cbAgree.setOnCheckedChangeListener { _, _ ->
            validateForm()
        }
    }

    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateForm()
            }
        }
        
        binding.etName.addTextChangedListener(textWatcher)
        binding.etPhone.addTextChangedListener(textWatcher)
        binding.etDetailAddress.addTextChangedListener(textWatcher)
    }

    private fun validateForm() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.tvSelectedAddress.text.toString()
        val detailAddress = binding.etDetailAddress.text.toString().trim()
        val isChecked = binding.cbAgree.isChecked
        
        // VISIT requires address, SHOP does not
        val isVisit = visitType == "방문 서비스"
        val addressValid = if (isVisit) {
            address != "주소를 선택해주세요" && detailAddress.isNotEmpty()
        } else {
            true // Address is optional for SHOP
        }
        
        val isValid = name.isNotEmpty() &&
                phone.isNotEmpty() &&
                addressValid &&
                isChecked
        
        binding.btnPay.apply {
            isEnabled = isValid
            backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (isValid) R.color.blue_4076FF else R.color.gray
            )
        }
    }

    private fun setupExpandableLayouts() {
        setupExpandable(binding.headerSellerInfo, binding.contentSellerInfo, binding.sellerArrow)
        setupExpandable(binding.headerPrivacyInfo, binding.contentPrivacyInfo, binding.privacyArrow)
        setupExpandable(binding.headerCancelRule, binding.contentCancelRule, binding.cancelArrow)
    }

    private fun setupExpandable(header: View, content: View, arrow: ImageView) {
        content.visibility = View.GONE
        arrow.rotation = 0f

        header.setOnClickListener {
            val expand = content.visibility == View.GONE

            if (expand) {
                content.visibility = View.VISIBLE
                arrow.animate().rotation(180f).setDuration(200).start()
            } else {
                content.visibility = View.GONE
                arrow.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun setupPayButton() {
        binding.btnPay.setOnClickListener {
            // Validate first
            validateForm()
            // Create reservation (PENDING) first, then pay
            createReservation()
        }
    }

    private fun createReservation() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val address = binding.tvSelectedAddress.text.toString()
        val detailAddress = binding.etDetailAddress.text.toString().trim()
        
        // Map visitType to serviceChannel
        val serviceChannel = when (visitType) {
            "직접 방문" -> "SHOP"
            "방문 서비스" -> "VISIT"
            else -> "SHOP"
        }
        
        // Prepare address fields (optional for SHOP, required for VISIT)
        val addressLine = if (address != "주소를 선택해주세요") address else null
        val addressDetail = if (detailAddress.isNotEmpty()) detailAddress else null
        
        val safeDate = selectedDate.trim()
        val safeTime = selectedTime.trim()
        val formattedTime = if (safeTime.length == 5) "$safeTime:00" else safeTime

        val request = ReservationRequest(
            shopId = shopId,
            treatmentId = treatmentId,
            staffId = staffId,
            reservedDate = safeDate,
            startTime = formattedTime,
            serviceChannel = serviceChannel,
            customerName = name,
            customerPhone = phone,
            visitAddressLine = addressLine,
            visitAddressDetail = addressDetail,
            termsAgreed = true,
            couponIssueId = selectedCoupon?.couponIssueId
        )

        Log.d("CustomerPayFragment", "Retry Reservation Request: $request")
        
        lifecycleScope.launch {
            try {
                binding.btnPay.isEnabled = false
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = api.createReservation(request)
                
                if (response.isSuccessful && response.body() != null) {
                    val reservationResponse = response.body()!!
                    if (reservationResponse.success && reservationResponse.data != null) {
                        val reservation = reservationResponse.data
                        
                        // Now launch payment with orderId provided by backend
                        val orderId = reservation.orderId
                        val orderName = serviceName
                        
                        val intent = Intent(requireContext(), TossPayActivity::class.java).apply {
                            putExtra(TossPayActivity.EXTRA_AMOUNT, reservation.finalAmount)
                            putExtra(TossPayActivity.EXTRA_ORDER_ID, orderId)
                            putExtra(TossPayActivity.EXTRA_ORDER_NAME, orderName)
                        }
                        paymentLauncher.launch(intent)
                        
                    } else {
                        Log.e("CustomerPayFragment", "Reservation creation failed: ${reservationResponse.message}")
                        Toast.makeText(
                            requireContext(),
                            "예약 생성 실패: ${reservationResponse.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding.btnPay.isEnabled = true
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CustomerPayFragment", "API call failed: ${response.code()}, Body: $errorBody")
                    Toast.makeText(
                        requireContext(),
                        "예약 생성에 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnPay.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("CustomerPayFragment", "Error creating reservation", e)
                Toast.makeText(
                    requireContext(),
                    "네트워크 오류가 발생했습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnPay.isEnabled = true
            }
        }
    }

    private fun setupAddressSearch() {
        binding.btnSearchAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddressSearchActivity::class.java)
            addressLauncher.launch(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 상태바 색상을 흰색으로 설정
        activity?.window?.statusBarColor = android.graphics.Color.WHITE
        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Save current state before view is destroyed
        savedName = binding.etName.text.toString()
        savedPhone = binding.etPhone.text.toString()
        savedAddress = binding.tvSelectedAddress.text.toString()
        savedDetailAddress = binding.etDetailAddress.text.toString()
        savedCheckboxState = binding.cbAgree.isChecked
        
        _binding = null
    }

    private fun setupReservationInfo() {
        val reservationInfo = binding.layoutReservationInfo
        
        // Set shop name
        reservationInfo.tvMachineName.text = shopName.ifEmpty { "매장명 없음" }
        
        // Format and set schedule (date and time)
        val formattedSchedule = formatScheduleText(selectedDate, selectedTime)
        reservationInfo.tvSchedule.text = formattedSchedule
        
        // Set service name
        reservationInfo.tvServiceInfo.text = serviceName.ifEmpty { "서비스 정보 없음" }
        
        // Set visit type
        reservationInfo.tvUseMethod.text = visitType.ifEmpty { "이용 방법 없음" }
    }
    
    private fun formatScheduleText(date: String, time: String): String {
        return try {
            // Parse date: "yyyy-MM-dd" -> "M.d (E)"
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("M.d (E)", java.util.Locale.KOREAN)
            val parsedDate = inputFormat.parse(date)
            val formattedDate = if (parsedDate != null) outputFormat.format(parsedDate) else date
            
            // Format time: remove seconds if present (HH:mm:ss -> HH:mm)
            val formattedTime = if (time.length > 5 && time.count { it == ':' } == 2) {
                time.substring(0, 5)
            } else {
                time
            }
            
            "$formattedDate • $formattedTime"
        } catch (e: Exception) {
            "$date • $time"
        }
    }
}

