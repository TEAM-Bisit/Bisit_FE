package com.example.bisit.data.api

import com.example.bisit.data.model.auth.AuthResponse
import com.example.bisit.data.model.auth.LoginRequest
import com.example.bisit.data.model.auth.LoginResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    
    @PATCH("/api/auth/logout")
    fun logout(): Call<AuthResponse>
    
    @DELETE("/api/auth/withdraw")
    fun withdraw(): Call<AuthResponse>
}