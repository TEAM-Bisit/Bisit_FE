package com.example.bisit.data.api

import com.example.bisit.data.model.mypage.SmsResponse
import com.example.bisit.data.model.mypage.SmsVerifyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SMSApiService {

    @POST("/api/sms/send")
    fun sendSms(@Body body: Map<String, String>): Call<SmsResponse>

    @POST("/api/sms/verify")
    fun verifySms(@Body body: Map<String, String>): Call<SmsVerifyResponse>
}
