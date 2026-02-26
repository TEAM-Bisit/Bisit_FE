package com.example.bisit.data.repository.todayReservation

import com.example.bisit.data.api.TodayReservationApiService
import com.example.bisit.data.model.todayReservation.*

class TodayReservationRepository(
    private val api: TodayReservationApiService
) {

    var onboardingMode: Boolean = false
        private set

    fun setOnboardingMode(enabled: Boolean) {
        onboardingMode = enabled
    }

    // 오늘의 예약 조회
    suspend fun getTodayReservations(
        shopId: Long,
        tab: String,
        sortBy: String
    ): CommonResponse<TodayReservationData> {

        if (onboardingMode) {
            return FakeTodayReservationData.pendingOneItemResponse(sortBy)
        }

        return api.getTodayReservations(
            shopId = shopId,
            tab = tab,
            sortBy = sortBy
        )
    }

    // 예약 상태 변경
    suspend fun changeStatus(
        reservationId: Long,
        body: ChangeStatusRequest
    ): CommonResponse<TodayReservationStatusData> {
        if (onboardingMode) {
            return FakeTodayReservationData.statusOkResponse()
        }
        return api.changeStatus(reservationId, body)
    }

    // 예약 거절
    suspend fun rejectReservation(
        reservationId: Long,
        body: RejectReservationRequest
    ): CommonResponse<TodayReservationStatusData> {
        if (onboardingMode) {
            return FakeTodayReservationData.statusOkResponse()
        }
        return api.rejectReservation(reservationId, body)
    }

    // 예약 승인
    suspend fun approveReservation(
        reservationId: Long
    ): CommonResponse<TodayReservationStatusData> {
        if (onboardingMode) {
            return FakeTodayReservationData.statusOkResponse()
        }
        return api.approveReservation(reservationId)
    }
}