package kr.bisit.app.data.api

import kr.bisit.app.data.model.auth.AuthResponse
import kr.bisit.app.data.model.auth.FindIdRequest
import kr.bisit.app.data.model.auth.LoginRequest
import kr.bisit.app.data.model.auth.LoginResponse
import kr.bisit.app.data.model.auth.PasswordResetRequest
import kr.bisit.app.data.model.auth.PasswordSendCodeRequest
import kr.bisit.app.data.model.auth.PasswordVerifyCodeRequest
import kr.bisit.app.data.model.auth.PasswordVerifyResponse
import kr.bisit.app.data.model.auth.ReissueResponse
import kr.bisit.app.data.model.signUp.SignUpRequest
import kr.bisit.app.data.model.signUp.SignUpResponse
import kr.bisit.app.data.model.todayReservation.CommonResponse
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

    @POST("/api/auth/find-id")
    fun findId(@Body request: FindIdRequest): Call<CommonResponse<String>>

    @POST("/api/auth/password/send-code")
    fun passwordSendCode(@Body request: PasswordSendCodeRequest): Call<CommonResponse<String>>

    @POST("/api/auth/password/verify-code")
    fun passwordVerifyCode(@Body request: PasswordVerifyCodeRequest): Call<CommonResponse<PasswordVerifyResponse>>

    @POST("/api/auth/password/reset")
    fun passwordReset(@Body request: PasswordResetRequest): Call<CommonResponse<String>>

    @GET("/api/auth/check/email")
    fun checkEmail(@Query("email") email: String): Call<CommonResponse<Boolean>>

    @POST("/api/auth/reissue")
    fun reissue(
        @Header("Cookie") refreshTokenCookie: String,
        @Query("authProvider") authProvider: String
    ): Call<ReissueResponse>
}