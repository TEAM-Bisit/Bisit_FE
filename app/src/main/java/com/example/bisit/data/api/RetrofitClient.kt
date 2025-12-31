package com.example.bisit.data.api

import android.content.Context
import android.util.Log
import com.example.bisit.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val NAVER_BASE_URL = "https://naveropenapi.apigw.ntruss.com/"
    private const val TAG = "RetrofitClient"

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


    val BASE_SERVER_URL: String = BuildConfig.BASE_SERVER_URL

    private var serverRetrofit: Retrofit? = null


    init {
        Log.d(TAG, "BASE_SERVER_URL = $BASE_SERVER_URL")
    }

    private fun getServerRetrofit(context: Context): Retrofit {
        val safeUrl = if (BASE_SERVER_URL.isBlank() || BASE_SERVER_URL == "null") {
            Log.e(TAG, "BASE_SERVER_URL이 설정되지 않았습니다. local.properties 확인하세요.")
            "http://localhost/"
        } else {
            BASE_SERVER_URL
        }

        if (serverRetrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor { chain ->
                            val request = chain.request()
                            Log.d(TAG, "🌐 API Request: ${request.method} ${request.url}")
                            val response = chain.proceed(request)
                            Log.d(TAG, "📡 API Response: ${response.code}")
                            if (!response.isSuccessful) {
                                Log.e(TAG, "❌ API Error: ${response.code} - ${response.message}")
                            }
                            response
                        }
                    }
                }
                .addInterceptor(loggingInterceptor)
                .addInterceptor(AuthInterceptor(context))
                .build()

            serverRetrofit = Retrofit.Builder()
                .baseUrl(safeUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }

        return serverRetrofit!!
    }

    fun getAuthApi(context: Context) =
        getServerRetrofit(context).create(AuthApiService::class.java)

    fun getTodayReservationApi(context: Context) =
        getServerRetrofit(context).create(TodayReservationApiService::class.java)

    fun getSmsApi(context: Context) =
        getServerRetrofit(context).create(SMSApiService::class.java)

    fun getReviewApi(context: Context) =
        getServerRetrofit(context).create(ReviewApiService::class.java)

    fun getShopApi(context: Context) =
        getServerRetrofit(context).create(CategoryApiService::class.java)

    fun getCustomerShopApi(context: Context) =
        getServerRetrofit(context).create(CustomerShopApiService::class.java)

    fun getPaymentApi(context: Context) =
        getServerRetrofit(context).create(PaymentApiService::class.java)

    fun getReservationApi(context: Context) =
        getServerRetrofit(context).create(ReservationApiService::class.java)

    fun getMemberApi(context: Context): MemberApiService {
        return getServerRetrofit(context).create(MemberApiService::class.java)
    }

    fun getCouponApi(context: Context): CouponApiService {
        return getServerRetrofit(context).create(CouponApiService::class.java)
    }
    
    fun getStaffManageApi(context: Context) =
        getServerRetrofit(context).create(StaffManageApiService::class.java)

    fun getShopBasicApi(context: Context) =
        getServerRetrofit(context).create(ShopBasicApiService::class.java)

    fun getShopPhotoApi(context: Context) =
        getServerRetrofit(context).create(ShopPhotoApiService::class.java)

    fun getShopDetailApi(context: Context) =
        getServerRetrofit(context).create(ShopDetailApiService::class.java)

    fun getShopAccountApi(context: Context) =
        getServerRetrofit(context).create(ShopAccountApiService::class.java)

    fun getShopRegisterApi(context: Context) =
        getServerRetrofit(context).create(ShopRegisterApiService::class.java)

    fun getShopNoticeApi(context: Context) =
        getServerRetrofit(context).create(ShopNoticeApiService::class.java)
}
