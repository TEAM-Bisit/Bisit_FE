package com.example.bisit.ui.customerPay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.bisit.R

class AddressSearchActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @Suppress("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_search)

        webView = findViewById(R.id.webView)

        val s: WebSettings = webView.settings
        s.javaScriptEnabled = true
        s.domStorageEnabled = true
        s.allowFileAccess = true
        s.javaScriptCanOpenWindowsAutomatically = true

        // WebViewClient / ChromeClient 반드시 기본값 설정!
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()

        // JS → Android 통신 브리지
        webView.addJavascriptInterface(AndroidBridge(), "Android")

        // 로컬 HTML 로드
        webView.loadUrl("file:///android_asset/postcode_naver.html")
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun onAddressSelected(
            display: String?,
            lat: String?,
            lng: String?,
            road: String?,
            jibun: String?
        ) {
            runOnUiThread {
                val intent = Intent().apply {
                    putExtra("selectedAddress", display ?: "")
                    putExtra("lat", lat ?: "")
                    putExtra("lng", lng ?: "")
                    putExtra("roadAddress", road ?: "")
                    putExtra("jibunAddress", jibun ?: "")
                }
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    override fun onDestroy() {
        try {
            webView.removeJavascriptInterface("Android")
            webView.stopLoading()

            // null 대입 불가 → 빈 객체로 재등록
            webView.webViewClient = WebViewClient()
            webView.webChromeClient = WebChromeClient()

            webView.destroy()
        } catch (_: Exception) {
        }

        super.onDestroy()
    }
}
