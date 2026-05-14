package kr.bisit.app.data.api

import kr.bisit.app.data.model.reservList.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservListApiService {

    /**
     * 매장 예약 목록 조회
     */
    @GET("/api/reservations/shops/{shopId}")
    suspend fun getReservationList(
        @Path("shopId") shopId: Long,
        @Query("date") date: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,

        @Query("sortDirection") sortDirection: String = "desc"
    ): Response<ReservationListResponse>


    /**
     * 매장 예약 상세 조회
     */
    @GET("/api/reservations/shops/detail/{reservationId}")
    suspend fun getReservationDetail(
        @Path("reservationId") reservationId: Long
    ): Response<ReservationDetailResponse>
}
