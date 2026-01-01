package com.example.bisit.data.repository.reservList

import android.content.Context
import com.example.bisit.data.api.ReservListApiService
import com.example.bisit.data.api.RetrofitClient

class ReservationRepository(context: Context) {

    private val api: ReservListApiService =
        RetrofitClient.getReservationListApi(context)

    suspend fun getReservationList(
        shopId: Long,
        date: String? = null,
        status: String? = null,
        page: Int = 0
    ) = api.getReservationList(
        shopId = shopId,
        date = date,
        status = status,
        page = page
    )

    suspend fun getReservationDetail(reservationId: Long) =
        api.getReservationDetail(reservationId)
}
