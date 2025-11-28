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

        window.statusBarColor = android.graphics.Color.WHITE
        window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        webView = findViewById(R.id.webView)

        val settings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true

        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.allowFileAccessFromFileURLs = true
        settings.allowUniversalAccessFromFileURLs = true

        webView.webViewClient = WebViewClient()

        webView.webChromeClient = WebChromeClient()

        webView.addJavascriptInterface(AndroidBridge(), "AndroidBridge")

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
