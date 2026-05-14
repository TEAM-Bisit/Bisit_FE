package kr.bisit.app.data.api

import kr.bisit.app.data.model.mypage.SmsResponse
import kr.bisit.app.data.model.mypage.SmsVerifyResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SMSApiService {

    @POST("/api/sms/send")
    fun sendSms(@Body body: Map<String, String>): Call<SmsResponse>

    @POST("/api/sms/verify")
    fun verifySms(@Body body: Map<String, String>): Call<SmsVerifyResponse>
}
