package com.example.bisit.ui.customerShop

import android.content.Context
import androidx.lifecycle.*
import com.example.bisit.data.model.customerShop.CustomerShopDetailItem
import com.example.bisit.data.repository.customerShop.CustomerShopRepository
import com.example.bisit.utils.TimeUtil
import kotlinx.coroutines.launch
import retrofit2.Response


import com.example.bisit.data.model.customerShop.CustomerShopIntroduceData

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
                val resp = repo.getShopDetail(context, shopId)

                if (resp.isSuccessful) {
                    val body = resp.body()
                    _shopData.value = body?.data

                    val createdAt = body?.data?.latestNotice?.createdAt
                    _noticeRelativeTime.value = TimeUtil.toRelativeTimeKorean(createdAt)
                } else {
                    _errorMsg.value = "서버 오류: ${resp.code()}"
                    _shopData.value = null
                }
            } catch (e: Exception) {
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
                val resp = repo.getShopIntroduce(context, shopId)
                if (resp.isSuccessful) {
                    _introduceData.value = resp.body()?.data
                }
            } catch (e: Exception) {
                // Ignore for now
            }
        }
    }
}