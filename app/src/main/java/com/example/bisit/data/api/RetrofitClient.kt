package com.example.bisit.data.api

import android.content.Context
import android.util.Log
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
    val BASE_SERVER_URL = BuildConfig.BASE_SERVER_URL

    private var serverRetrofit: Retrofit? = null

    private fun getServerRetrofit(context: Context): Retrofit {

        if (BASE_SERVER_URL.isBlank()) {
            Log.e("SERVER_ERROR", "BASE_SERVER_URL이 비어있습니다! local.properties 확인 필요")
            throw IllegalStateException("BASE_SERVER_URL이 비어 있습니다. local.properties에 값을 추가하세요.")
        }

        Log.d("SERVER_DEBUG", "Retrofit 생성됨 / BASE_SERVER_URL = $BASE_SERVER_URL")

        if (serverRetrofit == null) {
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context))
                .build()

            serverRetrofit = Retrofit.Builder()
                .baseUrl(BASE_SERVER_URL)   // ⭐ local.properties 값 그대로 사용
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

    fun getSmsApi(context: Context): SMSApiService {
        return getServerRetrofit(context).create(SMSApiService::class.java)
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
