package com.example.bisit.data.api

import com.example.bisit.data.model.reservation.CancelReservationRequest
import com.example.bisit.data.model.reservation.ReservationRequest
import com.example.bisit.data.model.reservation.ReservationResponse
import com.example.bisit.data.model.reservation.StaffAvailabilityResponse
import retrofit2.Response
import retrofit2.http.*
import com.example.bisit.data.model.todayReservation.ChangeStatusRequest

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
    ): Response<com.example.bisit.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/customers/completed")
    suspend fun getCompletedReservations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<com.example.bisit.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/customers/canceled")
    suspend fun getCanceledReservations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<com.example.bisit.data.model.reservation.ReservationListResponse>

    @GET("/api/reservations/{reservationId}/inquiry")
    suspend fun getReservationInquiry(
        @Path("reservationId") reservationId: Long
    ): Response<com.example.bisit.data.model.reservation.ReservationInquiryResponse>

    @GET("/api/reservations/{reservationId}")
    suspend fun getReservationDetail(
        @Path("reservationId") reservationId: Long
    ): Response<com.example.bisit.data.model.reservation.ReservationDetailResponse>

    @POST("/api/reservations/{reservationId}/cancel-by-customer")
    suspend fun cancelReservation(
        @Path("reservationId") reservationId: Long,
        @Body request: CancelReservationRequest
    ): Response<com.example.bisit.data.model.reservation.ReservationDetailResponse>

    @POST("/api/reservations/{reservationId}/confirm-by-customer")
    suspend fun confirmReservation(
        @Path("reservationId") reservationId: Long
    ): Response<com.example.bisit.data.model.reservation.ReservationDetailResponse>
}



