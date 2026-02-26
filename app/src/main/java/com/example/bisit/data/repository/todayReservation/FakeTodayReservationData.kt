package com.example.bisit.data.repository.todayReservation

import com.example.bisit.data.model.todayReservation.*

object FakeTodayReservationData {

    fun pendingOneItemResponse(
        tab: String = "pending",
        sortBy: String = "recent"
    ): CommonResponse<TodayReservationData> {

        val one = ReservationItem(
            reservationId = 9999L,
            status = "PENDING",
            serviceStatus = "WAITING",
            customerName = "김손님",
            treatmentName = "기본 시술",
            staffName = "김사장",
            visitAddressLine = "서울시 어딘가 1-1",
            reservedDate = "2026-01-01",
            startTime = "01:01"
        )

        val data = TodayReservationData(
            reservations = listOf(one),
            tab = tab,
            sortBy = sortBy,
            totalCount = 1
        )

        return CommonResponse(
            code = "200",
            success = true,
            message = "onboarding mock",
            data = data
        )
    }

    fun statusOkResponse(): CommonResponse<TodayReservationStatusData> {
        val data = TodayReservationStatusData(
            status = "APPROVED",
            serviceStatus = "READY",
            reservationId = 9999L,
            paymentStatus = "UNPAID",
            treatmentName = "기본 시술",
            reservedDate = "2026-01-01",
            startTime = "01:01",
            customerName = "김손님",
            customerPhone = "010-0000-0000",
            customerAddress = "서울시 어딘가 1-1",
            staffName = "김사장",
            price = 10000
        )

        return CommonResponse(
            code = "200",
            success = true,
            message = "onboarding mock status ok",
            data = data
        )
    }
}