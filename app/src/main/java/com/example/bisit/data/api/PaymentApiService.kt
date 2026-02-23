package com.example.bisit.data.api

import com.example.bisit.data.model.payment.PaymentConfirmData
import com.example.bisit.data.model.payment.PaymentConfirmRequest
import com.example.bisit.data.model.payment.VirtualAccountRequest
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.http.*

interface PaymentApiService {
    @POST("/api/payments/confirm")
    fun confirmPayment(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: PaymentConfirmRequest
    ): Call<CommonResponse<PaymentConfirmData>>
    
    @POST("/api/payments/virtual-account")
    fun createVirtualAccount(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: VirtualAccountRequest
    ): Call<CommonResponse<Any>>
    
    @POST("/api/payments/{paymentKey}/cancel")
    fun cancelPayment(
        @Path("paymentKey") paymentKey: String,
        @Body cancelRequest: Map<String, Any>
    ): Call<CommonResponse<Any>>
    
    @POST("/api/payments/fail")
    fun failPayment(@Body request: com.example.bisit.data.model.payment.PaymentFailRequest): Call<com.example.bisit.data.model.payment.PaymentFailResponse>
}
