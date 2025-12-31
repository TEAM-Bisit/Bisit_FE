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
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.TokenManager
import com.example.bisit.data.model.reservation.ReservationRequest
import com.example.bisit.databinding.FragmentCustomerPayBinding
import com.example.bisit.data.model.coupon.ApplicableCoupon
import kotlinx.coroutines.launch

class CustomerPayFragment : Fragment() {

    private var _binding: FragmentCustomerPayBinding? = null
    private val binding get() = _binding!!

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
    private var staffImage: String? = null
    private var reviewCount: Int = 0
    private var staffDescription: String? = null

    private var savedName: String = ""
    private var savedPhone: String = ""
    private var savedAddress: String = ""
    private var savedDetailAddress: String = ""
    private var savedCheckboxState: Boolean = false

    private var selectedCoupon: ApplicableCoupon? = null

    private val addressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && _binding != null) {
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
            if (paymentSuccess && isAdded) {
                Toast.makeText(requireContext(), "결제 및 예약이 완료되었습니다!", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
        } else if (isAdded) {
            Toast.makeText(requireContext(), "결제가 취소되었습니다.", Toast.LENGTH_SHORT).show()
            binding.btnPay.isEnabled = true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCustomerPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            staffImage = it.getString("staffImage")
            reviewCount = it.getInt("reviewCount", 0)
            staffDescription = it.getString("staffDescription")
        }

        restoreSavedState(savedInstanceState)
        setupReservationInfo()
        setupBackButton()
        setupCouponClick()
        setupCheckBox()
        setupExpandableLayouts()
        setupPayButton()
        setupAddressSearch()
        setupTextWatchers()

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ApplicableCoupon>("selectedCoupon")
            ?.observe(viewLifecycleOwner) { coupon ->
                selectedCoupon = coupon
                updatePriceWithCoupon()
            }

        loadApplicableCouponCount()
    }

    private fun loadApplicableCouponCount() {
        val currentContext = context ?: return
        // 🔥 [해결] 하드코딩된 1L 제거 -> TokenManager에서 마이페이지 API로 저장된 ID 가져오기
        val memberId = TokenManager.getMemberId(currentContext)

        if (memberId == -1L) {
            Log.e("CustomerPayFragment", "Member ID not found. User might not be logged in.")
            return
        }

        RetrofitClient.getCouponApi(currentContext).getApplicableCoupons(memberId, totalPrice)
            .enqueue(object : retrofit2.Callback<com.example.bisit.data.model.coupon.ApplicableCouponResponse> {
                override fun onResponse(call: retrofit2.Call<com.example.bisit.data.model.coupon.ApplicableCouponResponse>, response: retrofit2.Response<com.example.bisit.data.model.coupon.ApplicableCouponResponse>) {
                    if (_binding == null || !isAdded) return
                    if (response.isSuccessful && response.body()?.success == true) {
                        val count = response.body()?.data?.coupons?.size ?: 0
                        binding.tvCouponSelectCount.text = "${count}개"
                        binding.tvCouponSelectCount.visibility = if (count > 0 && selectedCoupon == null) View.VISIBLE else View.GONE
                    }
                }
                override fun onFailure(call: retrofit2.Call<com.example.bisit.data.model.coupon.ApplicableCouponResponse>, t: Throwable) {
                    if (isAdded) Log.e("CustomerPayFragment", "Network error loading coupon count", t)
                }
            })
    }

    private fun createReservation() {
        val currentContext = context ?: return
        if (_binding == null) return

        val serviceChannel = when (visitType) {
            "방문 서비스" -> "VISIT"
            else -> "SHOP"
        }

        val addressLine = if (binding.tvSelectedAddress.text != "주소를 선택해주세요") binding.tvSelectedAddress.text.toString() else null
        val addressDetail = binding.etDetailAddress.text.toString().trim().ifEmpty { null }
        val formattedTime = if (selectedTime.length == 5) "$selectedTime:00" else selectedTime

        val request = ReservationRequest(
            shopId = shopId,
            treatmentId = treatmentId,
            staffId = staffId,
            reservedDate = selectedDate.trim(),
            startTime = formattedTime,
            serviceChannel = serviceChannel,
            customerName = binding.etName.text.toString().trim(),
            customerPhone = binding.etPhone.text.toString().trim(),
            visitAddressLine = addressLine,
            visitAddressDetail = addressDetail,
            termsAgreed = true,
            couponIssueId = selectedCoupon?.couponIssueId
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnPay.isEnabled = false
                val api = RetrofitClient.getReservationApi(currentContext)
                val response = api.createReservation(request)

                if (_binding != null && response.isSuccessful && response.body()?.success == true) {
                    val reservation = response.body()!!.data!!
                    val intent = Intent(currentContext, TossPayActivity::class.java).apply {
                        putExtra(TossPayActivity.EXTRA_AMOUNT, reservation.finalAmount)
                        putExtra(TossPayActivity.EXTRA_ORDER_ID, reservation.orderId)
                        putExtra(TossPayActivity.EXTRA_ORDER_NAME, serviceName)
                    }
                    paymentLauncher.launch(intent)
                } else if (_binding != null) {
                    Toast.makeText(currentContext, "예약 생성 실패: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    binding.btnPay.isEnabled = true
                }
            } catch (e: Exception) {
                if (_binding != null) {
                    Toast.makeText(currentContext, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    binding.btnPay.isEnabled = true
                }
            }
        }
    }

    // --- 기존 UI 유지 코드 (생략 없이 모두 포함) ---
    private fun validateForm() {
        if (_binding == null) return
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val isChecked = binding.cbAgree.isChecked
        val isVisit = visitType == "방문 서비스"
        val addressValid = if (isVisit) binding.tvSelectedAddress.text != "주소를 선택해주세요" && binding.etDetailAddress.text.isNotEmpty() else true

        val isValid = name.isNotEmpty() && phone.isNotEmpty() && addressValid && isChecked
        binding.btnPay.apply {
            isEnabled = isValid
            backgroundTintList = ContextCompat.getColorStateList(requireContext(), if (isValid) R.color.blue_4076FF else R.color.gray)
        }
    }

    private fun setupReservationInfo() {
        val info = binding.layoutReservationInfo
        info.tvMachineName.text = shopName.ifEmpty { "매장명 없음" }
        info.tvStaffName.text = staffName.ifEmpty { "디자이너 없음" }
        info.tvSchedule.text = formatScheduleText(selectedDate, selectedTime)
        info.tvServiceInfo.text = serviceName.ifEmpty { "서비스 정보 없음" }
        if (!staffImage.isNullOrEmpty()) {
            com.bumptech.glide.Glide.with(this).load(staffImage).centerCrop().placeholder(R.drawable.img_designer).into(info.ivStaffProfile)
        }
    }

    private fun setupBackButton() { binding.btnBack.setOnClickListener { findNavController().popBackStack() } }
    private fun setupPayButton() { binding.btnPay.setOnClickListener { createReservation() } }
    private fun setupCheckBox() {
        binding.cbAgree.setOnCheckedChangeListener { _, _ -> validateForm() }
        binding.tvAgreeLabel.setOnClickListener { binding.cbAgree.isChecked = !binding.cbAgree.isChecked }
    }
    private fun setupAddressSearch() { binding.btnSearchAddress.setOnClickListener { addressLauncher.launch(Intent(requireContext(), AddressSearchActivity::class.java)) } }
    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateForm() }
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etPhone.addTextChangedListener(watcher)
        binding.etDetailAddress.addTextChangedListener(watcher)
    }

    private fun updatePriceWithCoupon() {
        if (_binding == null) return
        val fmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)
        if (selectedCoupon != null) {
            val finalPrice = totalPrice - (selectedCoupon?.expectedDiscount ?: 0)
            binding.tvDiscount.text = "-${fmt.format(selectedCoupon?.expectedDiscount)}원"
            binding.tvTotalPrice.text = "${fmt.format(finalPrice)}원"
            binding.tvCouponSelect.text = selectedCoupon?.name
            binding.tvCouponSelect.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_4076FF))
            binding.tvCouponSelectCount.visibility = View.GONE
        } else {
            binding.tvTotalPrice.text = "${fmt.format(totalPrice)}원"
            binding.tvCouponSelect.text = "적용 가능한 쿠폰"
            binding.tvCouponSelectCount.visibility = View.VISIBLE
        }
    }

    private fun setupCouponClick() {
        binding.layoutCoupon.setOnClickListener {
            val bundle = Bundle().apply { putInt("treatmentPrice", totalPrice) }
            findNavController().navigate(R.id.action_customerPayFragment_to_customerPayCouponFragment, bundle)
        }
        binding.tvProductPrice.text = "${java.text.NumberFormat.getNumberInstance().format(totalPrice)}원"
    }

    private fun setupExpandableLayouts() {
        setupExpandable(binding.headerSellerInfo, binding.contentSellerInfo, binding.sellerArrow)
        setupExpandable(binding.headerPrivacyInfo, binding.contentPrivacyInfo, binding.privacyArrow)
        setupExpandable(binding.headerCancelRule, binding.contentCancelRule, binding.cancelArrow)
    }

    private fun setupExpandable(header: View, content: View, arrow: ImageView) {
        content.visibility = View.GONE
        header.setOnClickListener {
            val expand = content.visibility == View.GONE
            content.visibility = if (expand) View.VISIBLE else View.GONE
            arrow.animate().rotation(if (expand) 180f else 0f).setDuration(200).start()
        }
    }

    private fun formatScheduleText(date: String, time: String): String {
        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("M.d (E)", java.util.Locale.KOREAN)
            val parsedDate = inputFormat.parse(date)
            val formattedDate = parsedDate?.let { outputFormat.format(it) } ?: date
            val formattedTime = if (time.length > 5) time.substring(0, 5) else time
            "$formattedDate • $formattedTime"
        } catch (e: Exception) { "$date • $time" }
    }

    private fun restoreSavedState(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            savedName = savedInstanceState.getString("name", "")
            savedPhone = savedInstanceState.getString("phone", "")
            savedAddress = savedInstanceState.getString("address", "주소를 선택해주세요")
            savedDetailAddress = savedInstanceState.getString("detailAddress", "")
            savedCheckboxState = savedInstanceState.getBoolean("checkbox", false)
        }
        binding.etName.setText(savedName); binding.etPhone.setText(savedPhone)
        if (savedAddress != "주소를 선택해주세요") binding.tvSelectedAddress.text = savedAddress
        binding.etDetailAddress.setText(savedDetailAddress); binding.cbAgree.isChecked = savedCheckboxState
        validateForm()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        _binding?.let {
            outState.putString("name", it.etName.text.toString())
            outState.putString("phone", it.etPhone.text.toString())
            outState.putString("address", it.tvSelectedAddress.text.toString())
            outState.putString("detailAddress", it.etDetailAddress.text.toString())
            outState.putBoolean("checkbox", it.cbAgree.isChecked)
        }
    }

    override fun onResume() { super.onResume(); activity?.window?.statusBarColor = android.graphics.Color.WHITE; activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}