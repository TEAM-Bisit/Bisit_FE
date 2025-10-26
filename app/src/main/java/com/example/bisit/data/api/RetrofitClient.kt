package com.example.bisit.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    private const val NCP_ACCESS_KEY_ID = "ncp_iam_BPAMKR3PLgKeBYxJmUID"
    private const val NCP_SECRET_KEY = "ncp_iam_BPKMKRX3UqkCGZPw1EAquRBDKK6hscFwiz"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", NCP_ACCESS_KEY_ID)
                .addHeader("X-NCP-APIGW-API-KEY", NCP_SECRET_KEY)
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