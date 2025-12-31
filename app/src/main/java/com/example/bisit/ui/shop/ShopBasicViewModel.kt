package com.example.bisit.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.*
import com.example.bisit.data.repository.shop.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopBasicViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ShopRepository(application)

    /* ===================== shopId 주입 ===================== */

    private val _shopId = MutableStateFlow<Long?>(null)

    fun setShopId(id: Long) {
        _shopId.value = id
    }

    private fun requireShopId(): Long {
        return _shopId.value
            ?: throw IllegalStateException("shopId가 설정되지 않았습니다")
    }

    /* ===================== 공통 상태 ===================== */

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== 샵 상세 ===================== */

    private val _shopDetail = MutableStateFlow<ShopDetailResponse?>(null)
    val shopDetail: StateFlow<ShopDetailResponse?> = _shopDetail

    /* ===================== 매장 소개 ===================== */

    private val _shopIntro = MutableStateFlow<ShopIntroduceResponse?>(null)
    val shopIntro: StateFlow<ShopIntroduceResponse?> = _shopIntro

    /* ===================== 정산 계좌 ===================== */

    private val _shopAccount = MutableStateFlow<ShopAccountResponse?>(null)
    val shopAccount: StateFlow<ShopAccountResponse?> = _shopAccount

    /* =====================================================
     * GET
     * ===================================================== */

    /** 샵 상세 조회 */
    fun fetchShopDetail() {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.getShopDetail(shopId)
                if (response.success) {
                    _shopDetail.value = response.data
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

    /** 매장 소개 조회 */
    fun fetchShopIntro() {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.getShopIntroduce(shopId)
                if (response.success) {
                    _shopIntro.value = response.data
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

    /** 정산 계좌 조회 */
    fun fetchShopAccount() {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.getShopAccount(shopId)
                if (response.success) {
                    _shopAccount.value = response.data
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

    /* =====================================================
     * PUT / POST
     * ===================================================== */

    /** 매장 소개 수정 */
    fun updateShopIntro(
        intro: String,
        serviceChannel: String,
        photoIds: List<Long>
    ) {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.updateShopIntroduce(
                    shopId = shopId,
                    request = ShopIntroduceRequest(
                        intro = intro,
                        serviceChannel = serviceChannel,
                        photoIds = photoIds
                    )
                )
                if (response.success) {
                    fetchShopIntro()
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

    /** 매장 기본 정보 수정 */
    fun updateShopBasicInfo(
        name: String,
        phone: String,
        addressLine: String,
        detailAddress: String
    ) {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.updateShopBasicInfo(
                    shopId = shopId,
                    request = ShopBasicInfoRequest(
                        name = name,
                        phone = phone,
                        addressLine = addressLine,
                        detailAddress = detailAddress
                    )
                )
                if (response.success) {
                    fetchShopDetail()
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

    /** 정산 계좌 등록 / 수정 */
    fun updateShopAccount(
        bankCode: String,
        accountNumber: String,
        accountHolder: String
    ) {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.registerOrUpdateAccount(
                    shopId = shopId,
                    request = ShopAccountRequest(
                        bankCode = bankCode,
                        accountNumber = accountNumber,
                        accountHolder = accountHolder
                    )
                )
                if (response.success) {
                    fetchShopAccount()
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

    /* ===================== 상태 제어 ===================== */

    fun clearError() {
        _errorMessage.value = null
    }
}
