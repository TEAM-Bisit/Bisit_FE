package com.example.bisit.data.api

import android.content.Context
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
            "/api/auth/check/email",
            "/api/auth/check/phone-number",
            "/api/auth/reissue"
        )

        // 해당 경로가 포함되어 있으면 토큰 없이 진행
        if (noAuthPaths.any { urlPath.contains(it) }) {
            return chain.proceed(originalRequest)
        }

        val token = TokenManager.getAccessToken(context)

        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}