package kr.bisit.app.data.api

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val urlPath = originalRequest.url.encodedPath

        // 토큰이 필요 없는 API 경로 목록 (로그인, 회원가입 등)
        val noAuthPaths = listOf(
            "/api/auth/login",
            "/api/auth/sign-up",
            "/api/sms/send", 
            "/api/sms/verify",
            "/api/auth/check/login-id",
            "/api/auth/check/phone-number",
            "/api/auth/find-id",
            "/api/auth/password/send-code",
            "/api/auth/password/verify-code",
            "/api/auth/password/reset",
            "/api/auth/check/email",
            "/api/auth/check/phone-number",
            "/api/auth/reissue",
            "/api/shops/category"
        )

        // 해당 경로가 포함되어 있으면 토큰 없이 진행
        val isNoAuth = noAuthPaths.any { urlPath.contains(it) }
        
        val token = TokenManager.getAccessToken(context)

        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            Log.d("AuthInterceptor", "🔑 Adding Token for: $urlPath")
            chain.proceed(newRequest)
        } else if (isNoAuth) {
            Log.d("AuthInterceptor", "🚀 Bypassing Auth (No Token) for: $urlPath")
            chain.proceed(originalRequest)
        } else {
            Log.d("AuthInterceptor", "⚠️ No Token found for: $urlPath")
            chain.proceed(originalRequest)
        }
    }
}