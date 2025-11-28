package com.example.bisit.data.api

import com.example.bisit.data.model.payment.PaymentConfirmData
import com.example.bisit.data.model.payment.PaymentConfirmRequest
import com.example.bisit.data.model.payment.VirtualAccountRequest
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApiService {
    @POST("/api/payments/confirm")
    fun confirmPayment(@Body request: PaymentConfirmRequest): Call<CommonResponse<PaymentConfirmData>>
    
    @POST("/api/payments/virtual-account")
    fun createVirtualAccount(@Body request: VirtualAccountRequest): Call<CommonResponse<Any>>
    
    @POST("/api/payments/{paymentKey}/cancel")
    fun cancelPayment(
        @Path("paymentKey") paymentKey: String,
        @Body cancelRequest: Map<String, Any>
    ): Call<CommonResponse<Any>>
}
