package com.example.bisit.data.api

import android.content.Context
import android.util.Log
import com.example.bisit.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val NAVER_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    private val naverClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NCP_KEY_ID)
                .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NCP_SECRET_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    val geocodingApi: NaverGeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NAVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(naverClient)
            .build()
            .create(NaverGeocodingApiService::class.java)
    }

    val BASE_SERVER_URL = BuildConfig.BASE_SERVER_URL

    private var serverRetrofit: Retrofit? = null

    private fun getServerRetrofit(context: Context): Retrofit {
        val safeUrl = if (BASE_SERVER_URL.isNullOrBlank() || BASE_SERVER_URL == "null") {
            Log.e("RetrofitClient", "BASE_SERVER_URL이 설정되지 않았습니다. local.properties를 확인하세요.")
            "http://localhost/"
        } else {
            BASE_SERVER_URL
        }

        if (serverRetrofit == null) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context))
                .build()

            serverRetrofit = Retrofit.Builder()
                .baseUrl(safeUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            Log.d("RetrofitClient", "서버 Retrofit 연결됨: $safeUrl")
        }
        return serverRetrofit!!
    }

    fun getAuthApi(context: Context): AuthApiService {
        return getServerRetrofit(context).create(AuthApiService::class.java)
    }

    fun getTodayReservationApi(context: Context): TodayReservationApiService {
        return getServerRetrofit(context).create(TodayReservationApiService::class.java)
    }

    fun getSmsApi(context: Context): SMSApiService {
        return getServerRetrofit(context).create(SMSApiService::class.java)
    }
}