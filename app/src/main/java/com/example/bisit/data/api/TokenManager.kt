package com.example.bisit.data.api

import android.content.Context

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_MEMBER_ID = "memberId"

    // [수정] 로그인 직후 토큰만 저장하는 함수
    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    // [추가] 마이페이지 조회 후 memberId만 별도로 저장하는 함수
    fun saveMemberId(context: Context, memberId: Long) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putLong(KEY_MEMBER_ID, memberId)
            .apply()
    }

    fun getMemberId(context: Context): Long {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getLong(KEY_MEMBER_ID, -1L)
    }

    fun getAccessToken(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens(context: Context) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().clear().apply()
    }
}