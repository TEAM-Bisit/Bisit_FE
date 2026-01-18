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

                // 1. [수정] 백엔드가 보내는 "access_token=" (언더바 포함) 형식을 감지합니다.
                if (url.contains("access_token=")) {
                    // [핵심] '#' 뒤에 토큰이 오므로, '#'을 '?'로 바꿔야 Uri.parse가 파라미터로 인식합니다.
                    val fixedUrl = url.replace("#", "?")
                    val uri = Uri.parse(fixedUrl)

                    // [수정] 키값도 백엔드 규격에 맞춰 "access_token", "refresh_token"으로 가져옵니다.
                    val accessToken = uri.getQueryParameter("access_token")
                    val refreshToken = uri.getQueryParameter("refresh_token")

                    if (accessToken != null) {
                        val intent = Intent().apply {
                            putExtra("ACCESS_TOKEN", accessToken)
                            putExtra("REFRESH_TOKEN", refreshToken)
                        }
                        setResult(RESULT_OK, intent)
                        finish() // 토큰을 챙겼으므로 웹뷰를 닫고 이전 화면으로 돌아갑니다.
                        return true // 웹뷰가 이 주소로 실제 접속하는 것을 막습니다. (400 에러 방지)
                    }
                }

                // 2. 외부 앱 실행 (카카오톡, 네이버 앱 등) 처리 로직
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