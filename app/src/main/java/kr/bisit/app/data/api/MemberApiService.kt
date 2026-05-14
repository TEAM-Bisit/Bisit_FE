package kr.bisit.app.data.api

import kr.bisit.app.data.model.member.FcmTokenRequest
import kr.bisit.app.data.model.member.MemberRoleRequest
import kr.bisit.app.data.model.member.MemberRoleResponse
import kr.bisit.app.data.model.member.MyPageResponse
import kr.bisit.app.data.model.member.MyProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT

interface MemberApiService {
    @GET("/api/members/my-page")
    fun getMyPage(): Call<MyPageResponse>

    @GET("/api/members/my-profile")
    fun getMyProfile(): Call<MyProfileResponse>

    @retrofit2.http.PATCH("/api/members/my-profile")
    fun updateMyProfile(@retrofit2.http.Body request: kr.bisit.app.data.model.member.MemberUpdateRequest): Call<kr.bisit.app.data.model.member.MemberUpdateResponse>

    @PATCH("/api/members/role")
    fun updateMemberRole(
        @Body request: MemberRoleRequest
    ): Call<MemberRoleResponse>

    @PUT("/api/members/fcm-token")
    fun updateFcmToken(
        @Body request: FcmTokenRequest
    ): Call<Void>
}
