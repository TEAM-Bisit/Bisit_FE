package kr.bisit.app.data.api

import kr.bisit.app.data.model.shop.*
import retrofit2.Response
import retrofit2.http.*

interface ShopNoticeApiService {

    /** 공지사항 목록 조회 */
    @GET("/api/shops/{shopId}/notices")
    suspend fun getShopNotices(
        @Path("shopId") shopId: Long,
        @Query("sortOrder") sortOrder: String = "desc"
    ): Response<ApiResponse<ShopNoticeListResponse>>

    /** 공지사항 생성 */
    @POST("/api/shops/{shopId}/notices")
    suspend fun createShopNotice(
        @Path("shopId") shopId: Long,
        @Body request: ShopNoticeRequest
    ): Response<ApiResponse<ShopNoticeResponse>>

    /** 공지사항 수정 */
    @PUT("/api/shops/{shopId}/notices/{noticeId}")
    suspend fun updateShopNotice(
        @Path("shopId") shopId: Long,
        @Path("noticeId") noticeId: Long,
        @Body request: ShopNoticeRequest
    ): Response<ApiResponse<ShopNoticeResponse>>

    /** 공지사항 삭제 */
    @DELETE("/api/shops/{shopId}/notices/{noticeId}")
    suspend fun deleteShopNotice(
        @Path("shopId") shopId: Long,
        @Path("noticeId") noticeId: Long
    ): Response<ApiResponse<String>>
}
