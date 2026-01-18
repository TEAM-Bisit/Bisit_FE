package com.example.bisit.data.model.reservation

data class ReservationData(
    @com.google.gson.annotations.SerializedName("reservationId", alternate = ["reservation_id"])
    val reservationId: Long,
    
    @com.google.gson.annotations.SerializedName("orderId", alternate = ["order_id"])
    val orderId: String,
    
    @com.google.gson.annotations.SerializedName("shopId", alternate = ["shop_id"])
    val shopId: Long,
    
    @com.google.gson.annotations.SerializedName("shopName", alternate = ["shop_name"])
    val shopName: String,
    
    @com.google.gson.annotations.SerializedName("treatmentId", alternate = ["treatment_id"])
    val treatmentId: Long,
    
    @com.google.gson.annotations.SerializedName("treatmentName", alternate = ["treatment_name"])
    val treatmentName: String,
    
    @com.google.gson.annotations.SerializedName("staffId", alternate = ["staff_id"])
    val staffId: Long,
    
    @com.google.gson.annotations.SerializedName("staffName", alternate = ["staff_name"])
    val staffName: String,
    
    @com.google.gson.annotations.SerializedName("reservedDate", alternate = ["reserved_date"])
    val reservedDate: String,
    
    @com.google.gson.annotations.SerializedName("startTime", alternate = ["start_time"])
    val startTime: String,
    
    @com.google.gson.annotations.SerializedName("endTime", alternate = ["end_time"])
    val endTime: String,
    
    @com.google.gson.annotations.SerializedName("listedPrice", alternate = ["listed_price"])
    val listedPrice: Int,
    
    @com.google.gson.annotations.SerializedName("discountAmount", alternate = ["discount_amount"])
    val discountAmount: Int,
    
    @com.google.gson.annotations.SerializedName("finalAmount", alternate = ["final_amount"])
    val finalAmount: Int,
    
    @com.google.gson.annotations.SerializedName("status")
    val status: String
)
