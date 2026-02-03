package com.example.bisit.ui.myPageOwner

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import com.example.bisit.databinding.DialogAddCouponBinding
import com.example.bisit.R
import java.util.Calendar

class DialogAddCoupon(
    context: Context,
    private val existingCoupon: OwnerCoupon? = null,
    private val onConfirm: (OwnerCoupon) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogAddCouponBinding
    private var isUpdatingFields = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogAddCouponBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Make dialog responsive to keyboard and add side margins
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupUI()
        setupListeners()
        existingCoupon?.let { prefillData(it) }
        validateFields()
    }

    private fun setupUI() {
        if (existingCoupon != null) {
            binding.btnRegister.text = "수정하기"
        }
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener { dismiss() }

        val mainWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateFields() }
        }

        binding.etCouponName.addTextChangedListener(mainWatcher)
        binding.etCouponDescription.addTextChangedListener(mainWatcher)

        // Exclusive logic for Amount and Percent
        binding.etDiscountAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingFields) return
                if (!s.isNullOrEmpty()) {
                    isUpdatingFields = true
                    binding.etDiscountPercent.setText("")
                    isUpdatingFields = false
                }
                validateFields()
            }
        })

        binding.etDiscountPercent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdatingFields) return
                if (!s.isNullOrEmpty()) {
                    isUpdatingFields = true
                    binding.etDiscountAmount.setText("")
                    isUpdatingFields = false
                }
                validateFields()
            }
        })

        binding.etExpiryDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etCouponName.text.toString()
            val amount = binding.etDiscountAmount.text.toString()
            val percent = binding.etDiscountPercent.text.toString()
            val description = binding.etCouponDescription.text.toString()
            val expiry = binding.etExpiryDate.text.toString()

            val displayValue = if (amount.isNotEmpty()) "${amount}원" else "${percent}%"

            val coupon = OwnerCoupon(
                id = existingCoupon?.id ?: System.currentTimeMillis().toString(),
                value = displayValue,
                name = name,
                description = description,
                remainingDays = 7, // Mock value
                expiryDate = expiry
            )
            onConfirm(coupon)
            dismiss()
        }
    }

    private fun prefillData(coupon: OwnerCoupon) {
        binding.etCouponName.setText(coupon.name)
        binding.etCouponDescription.setText(coupon.description)
        binding.etExpiryDate.setText(coupon.expiryDate)
        
        isUpdatingFields = true
        if (coupon.value.contains("%")) {
            binding.etDiscountPercent.setText(coupon.value.replace("%", ""))
            binding.etDiscountAmount.setText("")
        } else {
            binding.etDiscountAmount.setText(coupon.value.replace("원", "").replace(",", ""))
            binding.etDiscountPercent.setText("")
        }
        isUpdatingFields = false
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Using standard DatePickerDialog with a cleaner theme and ensuring correct behavior
        val datePickerDialog = DatePickerDialog(
            context,
            R.style.DatePickerTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val dateStr = String.format("%04d.%02d.%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etExpiryDate.setText(dateStr)
                validateFields()
            },
            year,
            month,
            day
        )
        
        // Ensure buttons are styled and logic is explicit if needed
        datePickerDialog.setOnShowListener {
            datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(android.graphics.Color.BLACK)
            datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(android.graphics.Color.GRAY)
        }
        
        datePickerDialog.show()
    }

    private fun validateFields() {
        if (isUpdatingFields) return
        val isNameFilled = binding.etCouponName.text.isNotEmpty()
        val isValueFilled = binding.etDiscountAmount.text.isNotEmpty() || binding.etDiscountPercent.text.isNotEmpty()
        val isDescFilled = binding.etCouponDescription.text.isNotEmpty()
        val isDateFilled = binding.etExpiryDate.text.isNotEmpty()

        binding.btnRegister.isEnabled = isNameFilled && isValueFilled && isDescFilled && isDateFilled
    }
}
