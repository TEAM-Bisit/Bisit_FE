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

    // 결제 전 예약 정보 확인
    suspend fun confirmReservation(
        customerId: Long,
        body: ConfirmReservationRequest
    ): CommonResponse<ConfirmReservationData> {
        return api.confirmReservation(customerId, body)
    }

    // 고객 예정된 예약 조회
    suspend fun getScheduledReservations(
        customerId: Long,
        page: Int,
        size: Int,
        sortDirection: String
    ): CommonResponse<TodayReservationListPageData> {
        return api.getScheduledReservations(customerId, page, size, sortDirection)
    }

    // 고객 완료 예약 조회
    suspend fun getCompletedReservations(
        customerId: Long,
        page: Int,
        size: Int,
        sortDirection: String
    ): CommonResponse<TodayReservationListPageData> {
        return api.getCompletedReservations(customerId, page, size, sortDirection)
    }

    // 고객 취소 예약 조회
    suspend fun getCanceledReservations(
        customerId: Long,
        page: Int,
        size: Int,
        sortDirection: String
    ): CommonResponse<TodayReservationListPageData> {
        return api.getCanceledReservations(customerId, page, size, sortDirection)
    }
}
