package com.example.bisit.data.model.payment

import com.google.gson.annotations.SerializedName

data class PaymentConfirmRequest(
    @SerializedName("paymentKey") val paymentKey: String,
    @SerializedName("orderId") val orderId: String,
    @SerializedName("amount") val amount: Int
)
