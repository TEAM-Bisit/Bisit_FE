package com.example.bisit.data.api

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.bisit.BuildConfig
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val NAVER_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"
    private const val NCP_ACCESS_KEY_ID = "ncp_iam_BPAMKR3PLgKeBYxJmUID"
    private const val NCP_SECRET_KEY = "ncp_iam_BPKMKRX3UqkCGZPw1EAquRBDKK6hscFwiz"

    private val naverClient = OkHttpClient.Builder()
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
            .baseUrl(NAVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(naverClient)
            .build()
            .create(NaverGeocodingApiService::class.java)
    }

    //local.properties에서 BASE_SERVER_URL = http:// ~~ 이렇게 추가하시면 됩니다
    private val BASE_SERVER_URL = BuildConfig.BASE_SERVER_URL
    private var serverRetrofit: Retrofit? = null

    private fun getServerRetrofit(context: Context): Retrofit {
        if (serverRetrofit == null) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context))
                .build()

            serverRetrofit = Retrofit.Builder()
                .baseUrl(BASE_SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }
        return serverRetrofit!!
    }

    fun getAuthApi(context: Context): AuthApiService {
        return getServerRetrofit(context).create(AuthApiService::class.java)
    }

    fun getTodayReservationApi(context: Context): TodayReservationApiService {
        return getServerRetrofit(context).create(TodayReservationApiService::class.java)
    }

    // 토큰이 안넘어가는 방식이기 때문에 위에 getTodatReservationApi로 대체하는게 좋을 것으로 보입니다!
//    val todayReservationApi: TodayReservationApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl(BASE_SERVER_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(TodayReservationApiService::class.java)
//    }
}
