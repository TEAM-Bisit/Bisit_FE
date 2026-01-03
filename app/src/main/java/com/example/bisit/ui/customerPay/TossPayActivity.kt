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
        val cookieManager = android.webkit.CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true) // 팝업 허용
            javaScriptCanOpenWindowsAutomatically = true
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url?.toString() ?: return false
                Log.d(TAG, "URL Loading: $url")

                if (url.startsWith("intent:")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        val packageManager = view?.context?.packageManager
                        if (intent.resolveActivity(packageManager!!) != null) {
                            startActivity(intent)
                            return true
                        }
                        
                        // Fallback to Play Store if app not installed
                        val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                        if (fallbackUrl != null) {
                            view.loadUrl(fallbackUrl)
                            return true
                        }
                        
                        val packageName = intent.`package`
                        if (packageName != null) {
                           startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                           return true
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Bad URI: $url", e)
                    }
                    return true
                }
                
                if (url.startsWith("market://")) {
                    try {
                         val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                         startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Market URL error", e)
                    }
                    return true
                }

                return when {
                    url.startsWith("tosspayments://success") -> {
                        handlePaymentSuccess(url)
                        true
                    }
                    url.startsWith("tosspayments://fail") -> {
                        handlePaymentFailure(url)
                        true
                    }
                    !url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:") -> {
                        // Handle other custom schemes
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            startActivity(intent)
                            true
                        } catch (e: Exception) {
                            Log.e(TAG, "Unknown scheme error", e)
                            true // Block unknown schemes to prevent crash
                        }
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
                    // errorCode -1 is ERROR_UNKNOWN, often Generic failure or minor connection drop
                    // Ignoring it visually might be better if the page eventually loads, but logging is good
                }
            }

            @SuppressLint("WebViewClientOnReceivedSslError")
            override fun onReceivedSslError(
                view: WebView?,
                handler: android.webkit.SslErrorHandler?,
                error: android.net.http.SslError?
            ) {
                Log.e(TAG, "SSL Error: ${error.toString()}")
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "DEBUG mode: Proceeding despite SSL error")
                    handler?.proceed()
                } else {
                    handler?.cancel()
                    Toast.makeText(this@TossPayActivity, "보안 연결에 실패했습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        
        // Console 로그 및 윈도우 생성(팝업) 처리를 위한 WebChromeClient
        webView.webChromeClient = object : android.webkit.WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                Log.d(TAG, "WebView Console: ${consoleMessage?.message()} -- Line: ${consoleMessage?.lineNumber()}")
                return true
            }

            override fun onCreateWindow(
                view: WebView?,
                isDialog: Boolean,
                isUserGesture: Boolean,
                resultMsg: android.os.Message?
            ): Boolean {
                Log.d(TAG, "onCreateWindow called")
                // 새 창을 위한 별도 WebView 생성
                val newWebView = WebView(this@TossPayActivity)
                newWebView.settings.javaScriptEnabled = true
                newWebView.settings.domStorageEnabled = true
                newWebView.settings.setSupportMultipleWindows(true)
                newWebView.settings.javaScriptCanOpenWindowsAutomatically = true

                // 쿠키 설정
                val cookieManager = android.webkit.CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(newWebView, true)
                
                // Dialog 등으로 띄워줄 수도 있지만, 여기서는 기존 WebView 위에 덮어쓰거나(addContentView),
                // 아니면 별도 처리가 필요. 
                // 가장 간단한 방법: Transport에 현재 WebView를 연결하거나, 
                // 새 다이얼로그에 WebView를 넣어 보여주기.
                
                // 구현 편의를 위해 Dialog 사용
                val dialog = android.app.Dialog(this@TossPayActivity).apply {
                    setContentView(newWebView)
                    window?.setLayout(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setOnDismissListener {
                        newWebView.destroy()
                    }
                    show()
                }

                newWebView.webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onCloseWindow(window: WebView?) {
                         dialog.dismiss()
                    }
                }
                
                newWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        // 새 창에서의 URL 처리
                        val url = request?.url?.toString() ?: return false
                        Log.d(TAG, "Popup URL: $url")
                        
                        // 외부 앱 실행 로직 등 동일하게 적용 가능
                        if (!url.startsWith("http") && !url.startsWith("javascript:")) {
                             try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                                return true
                            } catch (e: Exception) {
                                // Ignore
                            }
                        }
                        return false
                    }
                }

                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = newWebView
                resultMsg?.sendToTarget()
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
        
        Log.d(TAG, "Loading Payment Widget with clientKey: ${clientKey.take(10)}...")
        Log.d(TAG, "Amount: $amount, OrderId: $orderId, OrderName: $orderName")
        
        val html = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>토스페이먼츠 결제 위젯</title>
                <script src="https://js.tosspayments.com/v1/payment-widget"></script>
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
                        display: flex;
                        flex-direction: column;
                        height: 100vh;
                        background-color: #f5f5f5;
                    }
                    #payment-method {
                        background-color: white;
                        padding-top: 20px;
                    }
                    #agreement {
                        background-color: white;
                        margin-top: 10px;
                    }
                    .button-container {
                        padding: 20px;
                        background-color: white;
                        margin-top: auto;
                        box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
                    }
                    #payment-button {
                        width: 100%;
                        padding: 15px;
                        background-color: #3182f6;
                        color: white;
                        border: none;
                        border-radius: 8px;
                        font-size: 16px;
                        font-weight: 600;
                        cursor: pointer;
                        transition: background-color 0.2s;
                    }
                    #payment-button:active {
                        background-color: #1b64da;
                    }
                </style>
            </head>
            <body>
                <div id="payment-method"></div>
                <div id="agreement"></div>
                
                <div class="button-container">
                    <button id="payment-button" onclick="requestPayment()">결제하기</button>
                </div>

                <script>
                    const clientKey = '$clientKey';
                    const customerKey = 'CUSTOMER_KEY_TEST_${System.currentTimeMillis()}'; 
                    const paymentWidget = PaymentWidget(clientKey, customerKey);

                    const paymentMethodsWidget = paymentWidget.renderPaymentMethods(
                        '#payment-method', 
                        { value: $amount }
                    );
                    
                    paymentWidget.renderAgreement('#agreement');

                    function requestPayment() {
                        paymentWidget.requestPayment({
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
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        webView.loadDataWithBaseURL(
            "https://tosspayments.com", // Base URL updated for better compatibility
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
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "Payment confirmation failed: ${response.code()} - ${response.message()}\nBody: $errorBody")
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
