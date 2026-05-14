package kr.bisit.app.ui.myPageOwner

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import kr.bisit.app.databinding.DialogAddCouponBinding
import kr.bisit.app.R
import android.content.DialogInterface
import java.util.Calendar

import kr.bisit.app.data.model.coupon.CreateCouponRequest
import kr.bisit.app.data.model.coupon.OwnerCouponItem
import kr.bisit.app.data.model.coupon.UpdateCouponRequest
import java.text.SimpleDateFormat
import java.util.*

class DialogAddCoupon(
    context: Context,
    private val existingCoupon: OwnerCouponItem? = null,
    private val onConfirm: (Any) -> Unit
) : Dialog(context) {

    private var _binding: kr.bisit.app.databinding.DialogAddCouponBinding? = null
    private val binding get() = _binding!!
    private var isUpdatingFields = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = kr.bisit.app.databinding.DialogAddCouponBinding.inflate(android.view.LayoutInflater.from(context))
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
            binding.btnRegister.setText("수정하기")
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
            val amountStr = binding.etDiscountAmount.text.toString()
            val percentStr = binding.etDiscountPercent.text.toString()
            val description = binding.etCouponDescription.text.toString()
            val expiry = binding.etExpiryDate.text.toString()

            val amount = amountStr.toIntOrNull() ?: 0
            val percent = percentStr.toIntOrNull() ?: 0
            val type = if (amount > 0) "AMOUNT" else "PERCENT"

            // Convert expiry (YYYY.MM.DD) to ISO8601 (YYYY-MM-DDTHH:mm:ss.SSSZ)
            val validTo = convertToIso8601(expiry)
            val validFrom = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.format(Date())

            if (existingCoupon == null) {
                val request = CreateCouponRequest(
                    scope = "SHOP",
                    type = type,
                    name = name,
                    description = description,
                    amount = amount,
                    percent = percent,
                    minOrderAmount = 0, // Default or could add field
                    validFrom = validFrom,
                    validTo = validTo,
                    usageLimit = 0 // Default or could add field
                )
                onConfirm(request)
            } else {
                val request = UpdateCouponRequest(
                    type = type,
                    name = name,
                    description = description,
                    amount = amount,
                    percent = percent,
                    minOrderAmount = 0,
                    validFrom = validFrom,
                    validTo = validTo,
                    usageLimit = 0
                )
                onConfirm(request)
            }
            dismiss()
        }
    }

    private fun prefillData(coupon: OwnerCouponItem) {
        binding.etCouponName.setText(coupon.name)
        binding.etCouponDescription.setText(coupon.description)
        
        // Format ISO8601 to YYYY.MM.DD for display
        val displayDate = try {
            val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputSdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputSdf.parse(coupon.validTo)
            outputSdf.format(date!!)
        } catch (e: Exception) {
            coupon.validTo.split("T")[0].replace("-", ".")
        }
        binding.etExpiryDate.setText(displayDate)
        
        isUpdatingFields = true
        if (coupon.type == "PERCENT") {
            binding.etDiscountPercent.setText(coupon.percent.toString())
            binding.etDiscountAmount.setText("")
        } else {
            binding.etDiscountAmount.setText(coupon.amount.toString())
            binding.etDiscountPercent.setText("")
        }
        isUpdatingFields = false
    }

    private fun convertToIso8601(dateStr: String): String {
        return try {
            val inputSdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputSdf.parse(dateStr)
            val outputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            outputSdf.format(date!!)
        } catch (e: Exception) {
            dateStr.replace(".", "-") + "T23:59:59.000Z"
        }
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
        
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        
        // Show the dialog
        datePickerDialog.show()

        // Localize buttons after show - using setText for maximum compatibility
        datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setText("확인")
        datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setText("취소")
    }

    private fun validateFields() {
        if (isUpdatingFields) return
        val isNameFilled = binding.etCouponName.text.isNotEmpty()
        val isValueFilled = binding.etDiscountAmount.text.isNotEmpty() || binding.etDiscountPercent.text.isNotEmpty()
        val isDescFilled = binding.etCouponDescription.text.isNotEmpty()
        val isDateFilled = binding.etExpiryDate.text.isNotEmpty()

        binding.btnRegister.setEnabled(isNameFilled && isValueFilled && isDescFilled && isDateFilled)
    }

}
