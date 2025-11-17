package com.example.bisit.data.api

import com.example.bisit.data.model.todayReservation.*
import retrofit2.http.*

interface TodayReservationApiService {

    // 오늘의 예약 조회
    @GET("/api/reservations/shops/{shopId}/today")
    suspend fun getTodayReservations(
        @Path("shopId") shopId: Long,
        @Query("tab") tab: String = "pending",
        @Query("sortBy") sortBy: String = "recent"
    ): CommonResponse<TodayReservationData>


    // 예약 상태 변경
    @POST("/api/reservations/{reservationId}/status")
    suspend fun changeStatus(
        @Path("reservationId") reservationId: Long,
        @Body body: ChangeStatusRequest
    ): CommonResponse<TodayReservationStatusData>


    // 예약 거절
    @POST("/api/reservations/{reservationId}/reject")
    suspend fun rejectReservation(
        @Path("reservationId") reservationId: Long,
        @Body body: RejectReservationRequest
    ): CommonResponse<TodayReservationStatusData>


    // 예약 승인
    @POST("/api/reservations/{reservationId}/approve")
    suspend fun approveReservation(
        @Path("reservationId") reservationId: Long
    ): CommonResponse<TodayReservationStatusData>


    // 결제 전 예약 정보 확인
    @POST("/api/reservations/confirm")
    suspend fun confirmReservation(
        @Query("customerId") customerId: Long,
        @Body body: ConfirmReservationRequest
    ): CommonResponse<ConfirmReservationData>


    // 고객 예정된 예약 조회
    @GET("/api/reservations/customers/{customerId}/scheduled")
    suspend fun getScheduledReservations(
        @Path("customerId") customerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): CommonResponse<TodayReservationListPageData>


    // 고객 완료 예약 조회
    @GET("/api/reservations/customers/{customerId}/completed")
    suspend fun getCompletedReservations(
        @Path("customerId") customerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): CommonResponse<TodayReservationListPageData>


    // 고객 취소 예약 조회
    @GET("/api/reservations/customers/{customerId}/canceled")
    suspend fun getCanceledReservations(
        @Path("customerId") customerId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): CommonResponse<TodayReservationListPageData>
}
