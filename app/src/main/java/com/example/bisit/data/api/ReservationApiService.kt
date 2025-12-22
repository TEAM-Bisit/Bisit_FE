package com.example.bisit.data.api

import com.example.bisit.data.model.reservation.ReservationRequest
import com.example.bisit.data.model.reservation.ReservationResponse
import com.example.bisit.data.model.reservation.StaffAvailabilityResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
}
