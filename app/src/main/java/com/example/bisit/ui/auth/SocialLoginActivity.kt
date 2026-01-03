package com.example.bisit.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

        webView.settings.apply {
            javaScriptEnabled = true // 자바스크립트 허용
            domStorageEnabled = true // 로컬 저장소 허용 (카카오 로그인 필수)
            javaScriptCanOpenWindowsAutomatically = true // 팝업 허용
            setSupportMultipleWindows(true) // 멀티 윈도우 허용
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

                // 1. 서버가 정의한 성공 URL 가로채기
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

                // 2. 카카오톡 앱 실행 등 커스텀 스킴 처리 (매우 중요)
                if (url.startsWith("intent:") || url.startsWith("kakao") || url.startsWith("market:")) {
                    try {
                        val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                        startActivity(intent)
                        return true
                    } catch (e: Exception) {
                        // 카카오톡이 설치 안 된 경우 등 예외 처리
                        return false
                    }
                }
                return false
            }
        }

        // 쿠키 매니저 설정 (세션 유지용)
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.loadUrl("http://13.209.64.243:8080/oauth2/authorization/kakao")
    }
}