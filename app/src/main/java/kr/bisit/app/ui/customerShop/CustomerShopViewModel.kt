package kr.bisit.app.ui.customerShop

import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import kr.bisit.app.data.model.customerShop.CustomerShopDetailItem
import kr.bisit.app.data.repository.customerShop.CustomerShopRepository
import kr.bisit.app.utils.TimeUtil
import kotlinx.coroutines.launch
import retrofit2.Response


import kr.bisit.app.data.model.customerShop.CustomerShopIntroduceData

class CustomerShopViewModel(
    private val repo: CustomerShopRepository
) : ViewModel() {

    private val _shopData = MutableLiveData<CustomerShopDetailItem?>()
    val shopData: LiveData<CustomerShopDetailItem?> = _shopData

    private val _introduceData = MutableLiveData<CustomerShopIntroduceData?>()
    val introduceData: LiveData<CustomerShopIntroduceData?> = _introduceData

    private val _noticeRelativeTime = MutableLiveData<String?>()
    val noticeRelativeTime: LiveData<String?> = _noticeRelativeTime

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMsg = MutableLiveData<String?>()
    val errorMsg: LiveData<String?> = _errorMsg

    fun loadShop(context: Context, shopId: Long) {
        _isLoading.value = true
        _errorMsg.value = null

        viewModelScope.launch {
            try {
                Log.d("CustomerShopVM", "Loading shop detail for shopId: $shopId")
                val resp = repo.getShopDetail(context, shopId)

                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("CustomerShopVM", "Shop detail response: success=${body?.success}, data=${body?.data}")
                    _shopData.value = body?.data

                    val createdAt = body?.data?.latestNotice?.createdAt
                    Log.d("CustomerShopVM", "Notice createdAt: $createdAt")
                    _noticeRelativeTime.value = TimeUtil.toRelativeTimeKorean(createdAt)
                } else {
                    Log.e("CustomerShopVM", "Shop detail failed: ${resp.code()} - ${resp.message()}")
                    _errorMsg.value = "서버 오류: ${resp.code()}"
                    _shopData.value = null
                }
            } catch (e: Exception) {
                Log.e("CustomerShopVM", "Shop detail error", e)
                _errorMsg.value = "네트워크 오류: ${e.message}"
                _shopData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadShopIntroduce(context: Context, shopId: Long) {
        viewModelScope.launch {
            try {
                Log.d("CustomerShopVM", "Loading shop introduce for shopId: $shopId")
                val resp = repo.getShopIntroduce(context, shopId)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    Log.d("CustomerShopVM", "Shop introduce response: success=${body?.success}, data=${body?.data}")
                    _introduceData.value = body?.data
                } else {
                    // 403은 권한 문제로 introduce가 선택적 기능일 수 있으므로 에러 로그를 남기지 않음
                    if (resp.code() != 403) {
                        Log.e("CustomerShopVM", "Shop introduce failed: ${resp.code()} - ${resp.message()}")
                    } else {
                        Log.d("CustomerShopVM", "Shop introduce not available (403) - continuing without it")
                    }
                    _introduceData.value = null
                }
            } catch (e: Exception) {
                Log.e("CustomerShopVM", "Shop introduce error", e)
                _introduceData.value = null
            }
        }
    }

    // Services
    private val _servicesData = MutableLiveData<List<kr.bisit.app.data.model.shop.ServiceItem>>()
    val servicesData: LiveData<List<kr.bisit.app.data.model.shop.ServiceItem>> = _servicesData

    fun loadShopServices(context: Context, shopId: Long) {
        viewModelScope.launch {
            try {
                Log.d("CustomerShopVM", "Loading shop services for shopId: $shopId")
                // Fetch all or first page? Provided UI suggests a list. Let's fetch page 0 size 100 for now.
                val resp = repo.getShopTreatments(shopId, 0, 100)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body?.success == true) {
                        val items = body.data.treatments.content.map { 
                            kr.bisit.app.data.model.shop.ServiceItem(
                                name = it.name,
                                desc = it.description,
                                time = "${it.durationMin}분",
                                price = "${java.text.NumberFormat.getNumberInstance(java.util.Locale.KOREA).format(it.price)}원"
                            )
                        }
                        _servicesData.value = items
                    }
                } else {
                    Log.e("CustomerShopVM", "Shop services failed: ${resp.code()}")
                }
            } catch (e: Exception) {
                Log.e("CustomerShopVM", "Shop services error", e)
            }
        }
    }

    // Reviews
    private val _reviewsData = MutableLiveData<List<kr.bisit.app.data.model.shop.ReviewItem>>()
    val reviewsData: LiveData<List<kr.bisit.app.data.model.shop.ReviewItem>> = _reviewsData

    fun loadShopReviews(context: Context, shopId: Long) {
        // Repository returns Call, so we handle it with enqueue or convert. 
        // Ideally we should use suspend everywhere but let's stick to simple enqueue here inside VM or make repo suspend.
        // Actually, let's wrap it in viewModelScope but since it's Call, we use enqueue.
        
        repo.getShopReviews(context, shopId, 0, 100).enqueue(object : retrofit2.Callback<kr.bisit.app.data.model.review.ReviewListResponse> {
            override fun onResponse(
                call: retrofit2.Call<kr.bisit.app.data.model.review.ReviewListResponse>,
                response: Response<kr.bisit.app.data.model.review.ReviewListResponse>
            ) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val content = response.body()?.data?.reviews?.content ?: emptyList()
                    val items = content.map {
                        kr.bisit.app.data.model.shop.ReviewItem(
                            author = it.reviewerName ?: "익명",
                            content = it.content,
                            date = it.visitDate ?: it.createdAt?.substring(0, 10) ?: "",
                            serviceName = it.serviceName,
                            staffName = it.staffName,
                            rating = it.rating
                        )
                    }
                    _reviewsData.value = items
                } else {
                    Log.e("CustomerShopVM", "Shop reviews failed: ${response.code()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<kr.bisit.app.data.model.review.ReviewListResponse>, t: Throwable) {
                Log.e("CustomerShopVM", "Shop reviews error", t)
            }
        })
    }
}