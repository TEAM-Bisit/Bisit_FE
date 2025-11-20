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

        val settings = webView.settings

        // 기본
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true

        // asset + https 혼합 콘텐츠 허용
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        // asset에서 JS 로딩 허용
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        // 핵심
        webView.webViewClient = WebViewClient()

        // 🔥 popup / window.open 지원 필수
        webView.webChromeClient = WebChromeClient()

        // 🔥 HTML과 동일한 이름으로 JS 인터페이스 등록
        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

        // HTML 로드
        webView.loadUrl("file:///android_asset/daum_postcode.html")
    }

    inner class AndroidBridge {
        @JavascriptInterface
        fun onAddressSelected(address: String?) {
            val resultIntent = Intent().apply {
                putExtra("selectedAddress", address ?: "")
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}
