package com.example.bisit.ui.auth

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class SocialLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        val provider = intent.getStringExtra("PROVIDER") ?: "kakao"

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = true
            setSupportMultipleWindows(true)
        }

        webView.webViewClient = object : WebViewClient() {
            // [추가] 웹뷰가 페이지 로딩을 시작할 때마다 호출됨
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                // 로그캣(Logcat)에서 "WebViewURL"로 필터링해서 확인하세요.
                Log.d("WebViewURL", "로딩 시작: $url")
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("WebViewURL", "URL 이동 감지: $url")

                if (url.contains("accessToken=")) {
                    val uri = Uri.parse(url)
                    val intent = Intent().apply {
                        putExtra("ACCESS_TOKEN", uri.getQueryParameter("accessToken"))
                        putExtra("REFRESH_TOKEN", uri.getQueryParameter("refreshToken"))
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                    return true
                }

                if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        return false
                    }
                }
                return false
            }
        }

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        val authUrl = "http://13.209.64.243:8080/oauth2/authorization/$provider"
        Log.d("WebViewURL", "초기 호출 주소: $authUrl")
        webView.loadUrl(authUrl)
    }
}