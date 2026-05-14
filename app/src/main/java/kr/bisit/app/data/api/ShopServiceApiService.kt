package kr.bisit.app.data.api

import kr.bisit.app.data.model.shop.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ShopServiceApiService {

    /** 서비스(시술) 생성 */
    @Multipart
    @POST("/api/treatments/shops/{shopId}")
    suspend fun createTreatment(
        @Path("shopId") shopId: Long,
        @Part("request") request: RequestBody,
        @Part photo: MultipartBody.Part? = null
    ): Response<BaseResponse<TreatmentResponse>>

    /** 서비스(시술) 수정 */
    @Multipart
    @PUT("/api/treatments/{treatmentId}/shops/{shopId}")
    suspend fun updateTreatment(
        @Path("treatmentId") treatmentId: Long,
        @Path("shopId") shopId: Long,
        @Part("request") request: RequestBody,
        @Part photo: MultipartBody.Part? = null
    ): Response<BaseResponse<TreatmentResponse>>

    /** 서비스(시술) 삭제 (비활성화) */
    @DELETE("/api/treatments/{treatmentId}/shops/{shopId}")
    suspend fun deleteTreatment(
        @Path("treatmentId") treatmentId: Long,
        @Path("shopId") shopId: Long
    ): Response<BaseResponse<String>>

    /** 서비스(시술) 목록 조회 */
    @GET("/api/treatments/manager/shops/{shopId}")
    suspend fun getTreatments(
        @Path("shopId") shopId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BaseResponse<TreatmentListResponse>>
}
