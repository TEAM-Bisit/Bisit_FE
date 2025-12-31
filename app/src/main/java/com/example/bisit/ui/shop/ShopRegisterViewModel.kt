package com.example.bisit.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.ShopRegisterRequest
import com.example.bisit.data.repository.shop.ShopRegisterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopRegisterViewModel(
    private val repository: ShopRegisterRepository
) : ViewModel() {

    /* ===================== shopId 결과 ===================== */

    private val _shopId = MutableStateFlow<Long?>(null)
    val shopId: StateFlow<Long?> = _shopId

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== 가게 등록 ===================== */

    fun registerShop(
        businessRegNO: String,
        name: String,
        phone: String,
        addressLine: String,
        detailAddress: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.registerShop(
                    ShopRegisterRequest(
                        businessRegNO = businessRegNO,
                        name = name,
                        phone = phone,
                        addressLine = addressLine,
                        detailAddress = detailAddress
                    )
                )

                if (response.success) {
                    // 🔥 핵심: shopId만 저장
                    _shopId.value = response.data.shopId
                } else {
                    _errorMessage.value = response.message
                }

            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ===================== 상태 초기화 ===================== */

    fun clearShopId() {
        _shopId.value = null
    }
}
