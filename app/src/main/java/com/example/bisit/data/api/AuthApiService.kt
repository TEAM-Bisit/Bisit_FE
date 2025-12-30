package com.example.bisit.data.api

import com.example.bisit.data.model.auth.LoginRequest
import com.example.bisit.data.model.auth.LoginResponse
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @GET("/api/auth/check/login-id")
    fun checkLoginId(@Query("loginId") loginId: String): Call<CommonResponse<Boolean>>
}