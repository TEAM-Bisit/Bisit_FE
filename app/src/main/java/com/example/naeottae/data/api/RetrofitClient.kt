package com.example.naeottae.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"
    private const val ACCESS_KEY_ID = "ncp_iam_BPAMKR3PLgKeBYxJmUID"
    private const val SECRET_KEY = "nmKF0qGaHD2ivMcZ32YqPBYJN5RfZfAOlvlpheDC"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", ACCESS_KEY_ID)
                .addHeader("X-NCP-APIGW-API-KEY", SECRET_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    val geocodingApi: NaverGeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(NaverGeocodingApiService::class.java)
    }
}