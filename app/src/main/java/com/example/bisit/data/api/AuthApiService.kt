package com.example.bisit.data.api

import com.example.bisit.data.model.auth.AuthResponse
import com.example.bisit.data.model.auth.LoginRequest
import com.example.bisit.data.model.auth.LoginResponse
import com.example.bisit.data.model.auth.ReissueRequest
import com.example.bisit.data.model.auth.ReissueResponse
import com.example.bisit.data.model.signUp.SignUpRequest
import com.example.bisit.data.model.signUp.SignUpResponse
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApiService {
    @POST("/api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    
    @PATCH("/api/auth/logout")
    fun logout(): Call<AuthResponse>
    
    @DELETE("/api/auth/withdraw")
    fun withdraw(): Call<AuthResponse>


    @GET("/api/auth/check/login-id")
    fun checkLoginId(@Query("loginId") loginId: String): Call<CommonResponse<Boolean>>

    @POST("/api/auth/sign-up")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>

    @GET("/api/auth/check/phone-number")
    fun checkPhoneNumber(@Query("phoneNumber") phoneNumber: String): Call<CommonResponse<Boolean>>

    @GET("/api/auth/check/email")
    fun checkEmail(@Query("email") email: String): Call<CommonResponse<Boolean>>

    @POST("/api/auth/reissue")
    fun reissue(
        @Header("Cookie") refreshTokenCookie: String,
        @Query("authProvider") authProvider: String
    ): Call<ReissueResponse>
}