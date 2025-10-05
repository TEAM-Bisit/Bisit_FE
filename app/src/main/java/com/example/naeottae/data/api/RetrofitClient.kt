package com.example.naeottae.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"
    private const val CLIENT_ID = "0v8s5c7tw1"
    private const val CLIENT_SECRET = "nmKF0qGaHD2ivMcZ32YqPBYJN5RfZfAOlvlpheDC"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
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