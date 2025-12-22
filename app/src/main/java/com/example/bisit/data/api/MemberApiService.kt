package com.example.bisit.data.api

import com.example.bisit.data.model.member.MyPageResponse
import com.example.bisit.data.model.member.MyProfileResponse
import retrofit2.Call
import retrofit2.http.GET

interface MemberApiService {
    @GET("/api/members/my-page")
    fun getMyPage(): Call<MyPageResponse>

    @GET("/api/members/my-profile")
    fun getMyProfile(): Call<MyProfileResponse>
}
