package com.example.bisit.data.repository.todayReservation

import com.example.bisit.data.api.TodayReservationApiService
import com.example.bisit.data.model.todayReservation.*

class TodayReservationRepository(
    private val api: TodayReservationApiService
) {

    // 오늘의 예약 조회
    suspend fun getTodayReservations(
        shopId: Long,
        tab: String,
        sortBy: String
    ): CommonResponse<TodayReservationData> {
        return api.getTodayReservations(shopId, tab, sortBy)
    }

    // 예약 상태 변경
    suspend fun changeStatus(
        reservationId: Long,
        body: ChangeStatusRequest
    ): CommonResponse<TodayReservationStatusData> {
        return api.changeStatus(reservationId, body)
    }

    // 예약 거절
    suspend fun rejectReservation(
        reservationId: Long,
        body: RejectReservationRequest
    ): CommonResponse<TodayReservationStatusData> {
        return api.rejectReservation(reservationId, body)
    }

    // 예약 승인
    suspend fun approveReservation(
        reservationId: Long
    ): CommonResponse<TodayReservationStatusData> {
        return api.approveReservation(reservationId)
    }
}
