package com.example.bisit.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.BusinessHourItem
import com.example.bisit.data.model.shop.UpdateBusinessHourItem
import com.example.bisit.data.repository.shop.ShopBusinessHourRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopBusinessHourViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository =
        ShopBusinessHourRepository(application.applicationContext)

    /* ===================== 상태 ===================== */

    private val _shopId = MutableStateFlow<Long?>(null)

    private val _businessHours =
        MutableStateFlow<List<BusinessHourItem>>(emptyList())
    val businessHours: StateFlow<List<BusinessHourItem>> = _businessHours

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /* ===================== shopId ===================== */

    fun setShopId(shopId: Long) {
        _shopId.value = shopId
        fetchBusinessHours()
    }

    /* ===================== 조회 ===================== */

    fun fetchBusinessHours() {
        val id = _shopId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                repository.getBusinessHours(id)
            }.onSuccess {
                _businessHours.value = it
            }
            _isLoading.value = false
        }
    }

    /* ===================== 수정 ===================== */

    fun updateBusinessHours(
        items: List<UpdateBusinessHourItem>,
        onSuccess: (() -> Unit)? = null
    ) {
        val id = _shopId.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                repository.updateBusinessHours(id, items)
            }.onSuccess {
                onSuccess?.invoke()
                fetchBusinessHours()
            }
            _isLoading.value = false
        }
    }
}
