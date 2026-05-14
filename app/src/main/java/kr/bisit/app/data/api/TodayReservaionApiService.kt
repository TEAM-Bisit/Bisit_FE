package kr.bisit.app.data.api

import kr.bisit.app.data.model.todayReservation.*
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
}
