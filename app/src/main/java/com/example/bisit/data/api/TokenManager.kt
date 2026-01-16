package com.example.bisit.data.api

import android.content.Context

object TokenManager {
    private const val PREF_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_MEMBER_ID = "memberId"
    private const val KEY_AUTH_PROVIDER = "authProvider"
    private const val KEY_USER_ROLE = "userRole"

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

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

    fun saveAuthProvider(context: Context, provider: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit()
            .putString(KEY_AUTH_PROVIDER, provider)
            .apply()
    }

    fun getAuthProvider(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_AUTH_PROVIDER, null)
    }

    fun saveAccessToken(context: Context, token: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun saveRefreshToken(context: Context, token: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun saveUserRole(context: Context, role: String) {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(context: Context): String? {
        val pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return pref.getString(KEY_USER_ROLE, null)
    }
}