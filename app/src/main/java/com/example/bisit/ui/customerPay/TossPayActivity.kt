package com.example.bisit.ui.customerPay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bisit.BuildConfig
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.payment.PaymentConfirmData
import com.example.bisit.data.model.payment.PaymentConfirmRequest
import com.example.bisit.data.model.todayReservation.CommonResponse
import com.tosspayments.paymentsdk.PaymentWidget
import com.tosspayments.paymentsdk.model.PaymentCallback
import com.tosspayments.paymentsdk.model.TossPaymentResult
import com.tosspayments.paymentsdk.view.PaymentMethod
import com.tosspayments.paymentsdk.view.Agreement
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TossPayActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnPay: Button
    private lateinit var paymentMethod: PaymentMethod
    private lateinit var agreement: Agreement
    
    private lateinit var paymentWidget: PaymentWidget

    private var amount: Long = 0L
    private var orderId: String = ""
    private var orderName: String = ""
    private var customerKey: String = ""
    private var idempotencyKey: String = ""

    companion object {
        private const val TAG = "TossPayActivity"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_ORDER_NAME = "extra_order_name"
        const val RESULT_PAYMENT_SUCCESS = "payment_success"
        const val RESULT_PAYMENT_KEY = "payment_key"
        const val RESULT_ORDER_ID = "order_id"
        const val EXTRA_CUSTOMER_KEY = "extra_customer_key"
        const val EXTRA_IDEMPOTENCY_KEY = "extra_idempotency_key"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toss_pay)

        window.statusBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        progressBar = findViewById(R.id.progressBar)
        btnPay = findViewById(R.id.btnPay)
        paymentMethod = findViewById<PaymentMethod>(R.id.paymentMethodWidget)
        agreement = findViewById<Agreement>(R.id.agreementWidget)

        amount = intent.getLongExtra(EXTRA_AMOUNT, 0L)
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        orderName = intent.getStringExtra(EXTRA_ORDER_NAME) ?: "주문"
        customerKey = intent.getStringExtra(EXTRA_CUSTOMER_KEY) ?: "CUSTOMER_KEY_${System.currentTimeMillis()}"
        idempotencyKey = intent.getStringExtra(EXTRA_IDEMPOTENCY_KEY) ?: ""

        if (amount == 0L || orderId.isEmpty()) {
            Toast.makeText(this, "결제 정보가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupTossWidget()
    }

    private fun setupTossWidget() {
        val clientKey = BuildConfig.TOSS_CLIENT_KEY
        if (clientKey.isEmpty()) {
            Toast.makeText(this, "클라이언트 키 설정 오류", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        paymentWidget = PaymentWidget(
            activity = this,
            clientKey = clientKey,
            customerKey = customerKey
        )

        // 결제 금액 설정 및 렌더링
        // fun renderPaymentMethods(method: PaymentMethod, amount: Number, ...)
        paymentWidget.renderPaymentMethods(
            paymentMethod,
            amount.toInt()
        )

        // 약관 렌더링
        // fun renderAgreement(agreement: Agreement, ...)
        paymentWidget.renderAgreement(
            agreement
        )

        btnPay.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            btnPay.isEnabled = false
            
            // fun requestPayment(paymentInfo: PaymentMethod.PaymentInfo, paymentCallback: PaymentCallback)
            paymentWidget.requestPayment(
                paymentInfo = PaymentMethod.PaymentInfo(
                    orderId = orderId,
                    orderName = orderName
                ),
                paymentCallback = object : PaymentCallback {
                    override fun onPaymentSuccess(success: TossPaymentResult.Success) {
                        Log.d(TAG, "Payment success: $success")
                        confirmPayment(success.paymentKey, success.orderId, amount)
                    }

                    override fun onPaymentFailed(fail: TossPaymentResult.Fail) {
                        progressBar.visibility = View.GONE
                        btnPay.isEnabled = true
                        Log.e(TAG, "Payment failed: ${fail.errorCode}, ${fail.errorMessage}")
                        
                        // Call payment fail API to cancel reservation and restore coupon
                        handlePaymentFailure(orderId, fail.errorMessage ?: "결제 실패")
                    }
                }
            )
        }
    }

    private fun confirmPayment(paymentKey: String, orderId: String, amount: Long) {
        val request = PaymentConfirmRequest(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        Log.d(TAG, "🚀 Requesting payment confirmation with Idempotency-Key: $idempotencyKey")
        RetrofitClient.getPaymentApi(this).confirmPayment(idempotencyKey, request)
            .enqueue(object : Callback<CommonResponse<PaymentConfirmData>> {
                override fun onResponse(
                    call: Call<CommonResponse<PaymentConfirmData>>,
                    response: Response<CommonResponse<PaymentConfirmData>>
                ) {
                    progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val resultIntent = Intent().apply {
                            putExtra(RESULT_PAYMENT_SUCCESS, true)
                            putExtra(RESULT_PAYMENT_KEY, paymentKey)
                            putExtra(RESULT_ORDER_ID, orderId)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e(TAG, "Payment confirmation failed: ${response.code()}")
                        Toast.makeText(this@TossPayActivity, "결제 승인 실패", Toast.LENGTH_SHORT).show()
                        btnPay.isEnabled = true
                    }
                }

                override fun onFailure(call: Call<CommonResponse<PaymentConfirmData>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Payment confirmation API error", t)
                    Toast.makeText(this@TossPayActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    btnPay.isEnabled = true
                }
            })
    }
    
    private fun handlePaymentFailure(orderId: String, failReason: String) {
        val request = com.example.bisit.data.model.payment.PaymentFailRequest(
            orderId = orderId,
            failReason = failReason
        )
        
        RetrofitClient.getPaymentApi(this).failPayment(request)
            .enqueue(object : Callback<com.example.bisit.data.model.payment.PaymentFailResponse> {
                override fun onResponse(
                    call: Call<com.example.bisit.data.model.payment.PaymentFailResponse>,
                    response: Response<com.example.bisit.data.model.payment.PaymentFailResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()!!.data
                        val message = if (data?.couponRestored == true) {
                            "결제 실패: $failReason\n쿠폰이 복구되었습니다."
                        } else {
                            "결제 실패: $failReason"
                        }
                        Toast.makeText(this@TossPayActivity, message, Toast.LENGTH_LONG).show()
                        Log.d(TAG, "Payment failure processed: ${data?.message}")
                    } else {
                        Toast.makeText(this@TossPayActivity, "결제 실패: $failReason", Toast.LENGTH_LONG).show()
                    }
                }
                
                override fun onFailure(call: Call<com.example.bisit.data.model.payment.PaymentFailResponse>, t: Throwable) {
                    Log.e(TAG, "Payment fail API error", t)
                    Toast.makeText(this@TossPayActivity, "결제 실패: $failReason", Toast.LENGTH_LONG).show()
                }
            })
    }
}

