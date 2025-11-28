package com.example.bisit.ui.customerPay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerPayBinding

class CustomerPayFragment : Fragment() {

    private var _binding: FragmentCustomerPayBinding? = null
    private val binding get() = _binding!!

    // State variables to preserve form data
    private var savedName: String = ""
    private var savedPhone: String = ""
    private var savedAddress: String = ""
    private var savedDetailAddress: String = ""
    private var savedCheckboxState: Boolean = false

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
            val paymentKey = result.data?.getStringExtra(TossPayActivity.RESULT_PAYMENT_KEY) ?: ""
            val orderId = result.data?.getStringExtra(TossPayActivity.RESULT_ORDER_ID) ?: ""
            
            if (paymentSuccess) {
                android.widget.Toast.makeText(
                    requireContext(),
                    "결제가 완료되었습니다!\nPaymentKey: $paymentKey",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                // TODO: 결제 완료 후 예약 완료 화면으로 이동
                findNavController().popBackStack()
            }
        } else {
            android.widget.Toast.makeText(
                requireContext(),
                "결제가 취소되었습니다",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerPayBinding.inflate(inflater, container, false)

        // Restore saved state
        restoreSavedState(savedInstanceState)

        setupBackButton()
        setupCouponClick()
        setupCheckBox()
        setupExpandableLayouts()
        setupPayButton()
        setupAddressSearch()
        setupTextWatchers()

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
            findNavController().navigate(
                R.id.action_customerPayFragment_to_customerPayCouponFragment
            )
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
        
        // All fields must be filled and checkbox must be checked
        val isValid = name.isNotEmpty() &&
                phone.isNotEmpty() &&
                address != "주소를 선택해주세요" &&
                detailAddress.isNotEmpty() &&
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
            // 더미 데이터로 결제 테스트
            val dummyAmount = 10000
            val dummyOrderId = "TEST_ORDER_${System.currentTimeMillis()}"
            val dummyOrderName = "테스트 결제"
            
            val intent = Intent(requireContext(), TossPayActivity::class.java).apply {
                putExtra(TossPayActivity.EXTRA_AMOUNT, dummyAmount)
                putExtra(TossPayActivity.EXTRA_ORDER_ID, dummyOrderId)
                putExtra(TossPayActivity.EXTRA_ORDER_NAME, dummyOrderName)
            }
            paymentLauncher.launch(intent)
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
}

