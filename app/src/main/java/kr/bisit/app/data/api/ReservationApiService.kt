package kr.bisit.app.data.api

import kr.bisit.app.data.model.reservation.CancelReservationRequest
import kr.bisit.app.data.model.reservation.ReservationRequest
import kr.bisit.app.data.model.reservation.ReservationResponse
import kr.bisit.app.data.model.reservation.StaffAvailabilityResponse
import retrofit2.Response
import retrofit2.http.*

interface ReservationApiService {

    @GET("/api/reservations/staff-availability")
    suspend fun getStaffAvailability(
        @Query("staffId") staffId: Long,
        @Query("date") date: String
    ): Response<StaffAvailabilityResponse>

    @POST("/api/reservations")
    suspend fun createReservation(
        @Body request: ReservationRequest
    ): Response<ReservationResponse>

    @GET("/api/reservations/customers/scheduled")
    suspend fun getScheduledReservations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "asc"
    ): Response<kr.bisit.app.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/customers/completed")
    suspend fun getCompletedReservations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<kr.bisit.app.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/customers/canceled")
    suspend fun getCanceledReservations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<kr.bisit.app.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/{reservationId}/inquiry")
    suspend fun getReservationInquiry(
        @Path("reservationId") reservationId: Long
    ): Response<kr.bisit.app.data.model.reservation.ReservationInquiryResponse>

    @GET("/api/reservations/{reservationId}")
    suspend fun getReservationDetail(
        @Path("reservationId") reservationId: Long
    ): Response<kr.bisit.app.data.model.reservation.ReservationDetailResponse>

    @POST("/api/reservations/{reservationId}/cancel-by-customer")
    suspend fun cancelReservation(
        @Path("reservationId") reservationId: Long,
        @Body request: CancelReservationRequest
    ): Response<kr.bisit.app.data.model.reservation.ReservationDetailResponse>

    @POST("/api/reservations/{reservationId}/confirm-by-customer")
    suspend fun confirmReservation(
        @Path("reservationId") reservationId: Long
    ): Response<kr.bisit.app.data.model.reservation.ReservationDetailResponse>
}



