package com.example.bisit.data.api

import com.example.bisit.data.model.staffManage.*
import retrofit2.http.*

interface StaffManageApiService {

    // 사장님 → 직원 직접 추가
    @POST("/api/shops/{shopId}/staff/add")
    suspend fun addStaff(
        @Path("shopId") shopId: Long,
        @Body request: AddStaffRequest
    ): ApiResponse<StaffResponse>

    // 승인된 직원 삭제
    @DELETE("/api/shops/{shopId}/staff/{staffId}")
    suspend fun deleteStaff(
        @Path("shopId") shopId: Long,
        @Path("staffId") staffId: Long
    ): ApiResponse<String>

    // 직원 신청 승인 / 거절
    @PATCH("/api/shops/{shopId}/staff/{staffId}")
    suspend fun updateStaffStatus(
        @Path("shopId") shopId: Long,
        @Path("staffId") staffId: Long,
        @Query("status") status: StaffStatus
    ): ApiResponse<StaffResponse>

    // 직원 신청 목록 조회 (직원 요청 탭)
    @GET("/api/shops/{shopId}/staff/pending")
    suspend fun getPendingStaffs(
        @Path("shopId") shopId: Long
    ): ApiResponse<List<PendingStaffItem>>

    // 승인된 직원 목록 조회 (직원 목록 탭)
    @GET("/api/shops/{shopId}/staff/approved")
    suspend fun getApprovedStaffs(
        @Path("shopId") shopId: Long
    ): ApiResponse<List<ApprovedStaffItem>>
}
