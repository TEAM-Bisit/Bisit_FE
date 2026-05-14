package kr.bisit.app.data.repository.reservList

import android.content.Context
import kr.bisit.app.data.api.ReservListApiService
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.reservList.*
import retrofit2.Response

class ReservationRepository(context: Context) {

    private val api: ReservListApiService =
        RetrofitClient.getReservationListApi(context)

    private var onboardingMode: Boolean = false
    fun setOnboardingMode(enabled: Boolean) {
        onboardingMode = enabled
    }

    suspend fun getReservationList(
        shopId: Long,
        date: String? = null,
        status: String? = null,
        page: Int = 0
    ): Response<ReservationListResponse> {

        if (onboardingMode) {
            return Response.success(
                mockReservationListResponse(page = page, size = 10)
            )
        }

        return api.getReservationList(
            shopId = shopId,
            date = date,
            status = status,
            page = page
        )
    }

    suspend fun getReservationDetail(
        reservationId: Long
    ): Response<ReservationDetailResponse> {

        if (onboardingMode) {
            return Response.success(
                mockReservationDetailResponse(reservationId)
            )
        }

        return api.getReservationDetail(reservationId)
    }

    /* ===================== Mock Data ===================== */

    private fun mockReservationListResponse(page: Int, size: Int): ReservationListResponse {
        val items = listOf(
            ReservationListItem(
                reservationId = 1001L,
                status = "CONFIRMED",
                serviceStatus = "CUSTOMER_CONFIRMED",
                customerName = "김고객",
                treatmentName = "커트",
                staffName = "디자이너 수아",
                reservedDate = "2026.03.01",
                startTime = "14:00"
            ),
            ReservationListItem(
                reservationId = 1002L,
                status = "CONFIRMED",
                serviceStatus = "CONFIRMED",
                customerName = "이손님",
                treatmentName = "염색",
                staffName = "디자이너 민지",
                reservedDate = "2026.03.01",
                startTime = "15:30"
            )
        )

        return ReservationListResponse(
            success = true,
            code = "MOCK_OK",
            message = "온보딩 목데이터",
            data = ReservationListData(
                reservations = ReservationPage(
                    content = items,
                    totalPages = 1,
                    totalElements = items.size,
                    currentPage = page,
                    size = size,
                    hasNext = false
                )
            )
        )
    }

    private fun mockReservationDetailResponse(reservationId: Long): ReservationDetailResponse {
        return ReservationDetailResponse(
            success = true,
            code = "MOCK_OK",
            message = "온보딩 상세 목데이터",
            data = ReservationDetailData(
                status = "CONFIRMED",
                serviceStatus = "CUSTOMER_CONFIRMED",
                reservationId = reservationId,
                reservationPaymentStatus = "PAID",
                treatmentName = "커트",
                reservedDate = "2026.03.01",
                startTime = "14:00",
                customerName = "김고객",
                customerPhone = "010-1234-5678",
                customerAddress = "서울시 어딘가",
                staffName = "디자이너 수아",
                price = 25000
            )
        )
    }
}