package com.example.bisit.data.api

import com.example.bisit.data.model.member.MemberRoleRequest
import com.example.bisit.data.model.member.MemberRoleResponse
import com.example.bisit.data.model.member.MyPageResponse
import com.example.bisit.data.model.member.MyProfileResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface MemberApiService {
    @GET("/api/members/my-page")
    fun getMyPage(): Call<MyPageResponse>

    @GET("/api/members/my-profile")
    fun getMyProfile(): Call<MyProfileResponse>

    @retrofit2.http.PATCH("/api/members/my-profile")
    fun updateMyProfile(@retrofit2.http.Body request: com.example.bisit.data.model.member.MemberUpdateRequest): Call<com.example.bisit.data.model.member.MemberUpdateResponse>

    @PATCH("/api/members/role")
    fun updateMemberRole(
        @Body request: MemberRoleRequest
    ): Call<MemberRoleResponse>
}
