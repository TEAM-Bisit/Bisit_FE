package com.example.bisit.ui.customerPay

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bisit.BuildConfig
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.payment.PaymentConfirmData
import com.example.bisit.data.model.payment.PaymentConfirmRequest
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TossPayActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var amount: Int = 0
    private var orderId: String = ""
    private var orderName: String = ""

    companion object {
        private const val TAG = "TossPayActivity"
        const val EXTRA_AMOUNT = "extra_amount"
        const val EXTRA_ORDER_ID = "extra_order_id"
        const val EXTRA_ORDER_NAME = "extra_order_name"
        const val RESULT_PAYMENT_SUCCESS = "payment_success"
        const val RESULT_PAYMENT_KEY = "payment_key"
        const val RESULT_ORDER_ID = "order_id"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_toss_pay)

        window.statusBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        amount = intent.getIntExtra(EXTRA_AMOUNT, 0)
        orderId = intent.getStringExtra(EXTRA_ORDER_ID) ?: ""
        orderName = intent.getStringExtra(EXTRA_ORDER_NAME) ?: "주문"

        if (amount == 0 || orderId.isEmpty()) {
            Log.e(TAG, "Invalid payment data: amount=$amount, orderId=$orderId")
            Toast.makeText(this, "결제 정보가 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()
        loadPaymentPage()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
            javaScriptCanOpenWindowsAutomatically = true
            // CORS 에러 방지를 위한 설정
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d(TAG, "URL Loading: $url")

                return when {
                    url.startsWith("tosspayments://success") -> {
                        handlePaymentSuccess(url)
                        true
                    }
                    url.startsWith("tosspayments://fail") -> {
                        handlePaymentFailure(url)
                        true
                    }
                    else -> false
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                Log.d(TAG, "Page finished loading: $url")
            }
            
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: android.webkit.WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Log.e(TAG, "WebView Error: ${error?.description}, errorCode: ${error?.errorCode}")
                // ORB 에러는 무시 (외부 스크립트 로딩 시 정상적으로 발생할 수 있음)
                if (error?.errorCode != -1) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@TossPayActivity,
                        "페이지 로드 오류: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Console 로그를 확인하기 위한 WebChromeClient
        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                Log.d(TAG, "WebView Console: ${consoleMessage?.message()} -- Line: ${consoleMessage?.lineNumber()}")
                return true
            }
        }
    }

    private fun loadPaymentPage() {
        val clientKey = BuildConfig.TOSS_CLIENT_KEY

        if (clientKey.isNullOrEmpty() || clientKey == "null" || clientKey == "") {
            Log.e(TAG, "TOSS_CLIENT_KEY is empty or null!")
            Toast.makeText(this, "결제 설정 오류: 클라이언트 키가 없습니다", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        Log.d(TAG, "Loading payment page with clientKey: ${clientKey.take(10)}...")
        Log.d(TAG, "Amount: $amount, OrderId: $orderId, OrderName: $orderName")
        
        val html = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>토스페이먼츠 결제</title>
                <script src="https://js.tosspayments.com/v2/standard"></script>
            </head>
            <body>
                <script>
                    const clientKey = '$clientKey';
                    const tossPayments = TossPayments(clientKey);
                    
                    const payment = tossPayments.payment({
                        customerKey: 'CUSTOMER_KEY_TEST_${System.currentTimeMillis()}'
                    });
                    
                    payment.requestPayment({
                        method: 'CARD',
                        amount: {
                            currency: 'KRW',
                            value: $amount
                        },
                        orderId: '$orderId',
                        orderName: '$orderName',
                        successUrl: 'tosspayments://success',
                        failUrl: 'tosspayments://fail',
                        customerEmail: 'test@test.com',
                        customerName: '테스트',
                        customerMobilePhone: '01012345678'
                    }).catch(function(error) {
                        console.error('Payment error:', error);
                        window.location.href = 'tosspayments://fail?code=' + error.code + '&message=' + encodeURIComponent(error.message);
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(
            null,  // CORS 에러 방지를 위해 null로 설정
            html,
            "text/html",
            "UTF-8",
            null
        )
    }

    private fun handlePaymentSuccess(url: String) {
        try {
            val uri = Uri.parse(url)
            val paymentKey = uri.getQueryParameter("paymentKey") ?: ""
            val returnedOrderId = uri.getQueryParameter("orderId") ?: ""
            val returnedAmount = uri.getQueryParameter("amount")?.toIntOrNull() ?: 0

            Log.d(TAG, "Payment Success - paymentKey: $paymentKey, orderId: $returnedOrderId, amount: $returnedAmount")

            if (returnedAmount != amount) {
                Log.e(TAG, "Amount mismatch: expected=$amount, received=$returnedAmount")
                Toast.makeText(this, "결제 금액이 일치하지 않습니다", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }

            confirmPayment(paymentKey, returnedOrderId, returnedAmount)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling payment success", e)
            Toast.makeText(this, "결제 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun confirmPayment(paymentKey: String, orderId: String, amount: Int) {
        progressBar.visibility = View.VISIBLE
        
        val request = PaymentConfirmRequest(
            paymentKey = paymentKey,
            orderId = orderId,
            amount = amount
        )

        RetrofitClient.getPaymentApi(this).confirmPayment(request)
            .enqueue(object : Callback<CommonResponse<PaymentConfirmData>> {
                override fun onResponse(
                    call: Call<CommonResponse<PaymentConfirmData>>,
                    response: Response<CommonResponse<PaymentConfirmData>>
                ) {
                    progressBar.visibility = View.GONE
                    
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        Log.d(TAG, "Payment confirmed successfully: $data")
                        
                        val resultIntent = Intent().apply {
                            putExtra(RESULT_PAYMENT_SUCCESS, true)
                            putExtra(RESULT_PAYMENT_KEY, paymentKey)
                            putExtra(RESULT_ORDER_ID, orderId)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    } else {
                        Log.e(TAG, "Payment confirmation failed: ${response.code()} - ${response.message()}")
                        Toast.makeText(
                            this@TossPayActivity,
                            "결제 승인에 실패했습니다: ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                }

                override fun onFailure(call: Call<CommonResponse<PaymentConfirmData>>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Payment confirmation API error", t)
                    Toast.makeText(
                        this@TossPayActivity,
                        "결제 승인 중 오류가 발생했습니다: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            })
    }

    private fun handlePaymentFailure(url: String) {
        try {
            val uri = Uri.parse(url)
            val code = uri.getQueryParameter("code") ?: "UNKNOWN"
            val message = uri.getQueryParameter("message") ?: "결제에 실패했습니다"

            Log.e(TAG, "Payment Failed - code: $code, message: $message")
            Toast.makeText(this, "결제 실패: $message", Toast.LENGTH_LONG).show()
            
            setResult(Activity.RESULT_CANCELED)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error handling payment failure", e)
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
            setResult(Activity.RESULT_CANCELED)
        }
    }
}
