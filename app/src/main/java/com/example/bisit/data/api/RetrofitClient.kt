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
    private const val OPEN_NAVER_BASE_URL = "https://openapi.naver.com/"
    private const val TAG = "RetrofitClient"
 
    private val ncpClient by lazy {
        Log.i("NaverAuthDebug", "🚀 Initializing ncpClient (NCP - Geocoding)")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", BuildConfig.NAVER_MAP_CLIENT_ID)
                    .addHeader("X-NCP-APIGW-API-KEY", BuildConfig.NAVER_MAP_CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val openNaverClient by lazy {
        Log.i("NaverAuthDebug", "🚀 Initializing openNaverClient (Developers - Search)")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("X-Naver-Client-Id", BuildConfig.NAVER_DEV_CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", BuildConfig.NAVER_DEV_CLIENT_SECRET)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()
    }
 
    val geocodingApi: NaverGeocodingApiService by lazy {
        Retrofit.Builder()
            .baseUrl(NAVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(ncpClient)
            .build()
            .create(NaverGeocodingApiService::class.java)
    }
 
    val naverSearchApi: NaverSearchApiService by lazy {
        Retrofit.Builder()
            .baseUrl(OPEN_NAVER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(openNaverClient)
            .build()
            .create(NaverSearchApiService::class.java)
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
                            val authHeader = request.header("Authorization")
                            if (authHeader != null) {
                                val truncated = if (authHeader.length > 20) authHeader.substring(0, 15) + "..." else authHeader
                                Log.d(TAG, "🔑 Auth: $truncated")
                            }
                            val response = chain.proceed(request)
                            Log.d(TAG, "📡 API Response: ${response.code}")
                            
                            // 403 에러는 /introduce 엔드포인트의 경우 선택적 기능이므로 에러 로그 출력 안 함
                            if (!response.isSuccessful) {
                                val isIntroduceEndpoint = request.url.toString().contains("/introduce")
                                if (response.code == 403 && isIntroduceEndpoint) {
                                    // 403 on /introduce is expected, don't log as error
                                } else {
                                    Log.e(TAG, "❌ API Error: ${response.code} - ${response.message}")
                                }
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

    fun getStoreApi(context: Context) =
        getServerRetrofit(context).create(ShopApiService::class.java)

    fun getMapApi(context: Context) =
        getServerRetrofit(context).create(MapApiService::class.java)
        
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

    fun getShopServiceApi(context: Context) =
        getServerRetrofit(context).create(ShopServiceApiService::class.java)

    fun getReservationListApi(context: Context) =
        getServerRetrofit(context).create(ReservListApiService::class.java)

    fun getReviewManageApi(context: Context) =
        getServerRetrofit(context).create(ReviewManageApi::class.java)
}
