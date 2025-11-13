package com.example.bisit.ui.reservList.model

import java.io.Serializable

data class ReservListItem(
    val id: String,
    val userId: String,
    val serviceName: String,
    val customerName: String,
    val dateTime: String,
    val price: Int,
    val address: String,
    val phone: String,
    val status: String,
) : Serializable
