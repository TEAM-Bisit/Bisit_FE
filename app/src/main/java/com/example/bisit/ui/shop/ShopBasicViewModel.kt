package com.example.bisit.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.*
import com.example.bisit.data.repository.shop.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ShopBasic 화면 전용 ViewModel
 * - 매장 기본 정보
 * - 소개
 * - 계좌
 * - 영업시간(조회)
 */
class ShopBasicViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ShopRepository(application)

    /* ===================== shopId 주입 ===================== */

    private val _shopId = MutableStateFlow<Long?>(null)

    fun setShopId(id: Long) {
        _shopId.value = id
    }

    private fun requireShopId(): Long =
        _shopId.value ?: throw IllegalStateException("shopId가 설정되지 않았습니다")

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

    /* ===================== 영업시간 (UI 전용) ===================== */

    private val _shopOpenHour = MutableStateFlow<ShopOpenHourUiModel?>(null)
    val shopOpenHour: StateFlow<ShopOpenHourUiModel?> = _shopOpenHour

    /* =====================================================
     * GET
     * ===================================================== */

    /** 샵 상세 조회 (영업시간 가공 포함) */
    fun fetchShopDetail() {
        viewModelScope.launch {
            val shopId = requireShopId()
            _isLoading.value = true
            try {
                val response = repository.getShopDetail(shopId)
                if (response.success) {
                    val detail = response.data
                    _shopDetail.value = detail

                    // ===== 영업시간 가공 =====
                    val todayOpen = detail.weeklyBusinessHours.firstOrNull {
                        !it.isClosed
                    }

                    val weeklyTexts = detail.weeklyBusinessHours.map { hour ->
                        val dayKr = when (hour.day) {
                            "MONDAY" -> "월"
                            "TUESDAY" -> "화"
                            "WEDNESDAY" -> "수"
                            "THURSDAY" -> "목"
                            "FRIDAY" -> "금"
                            "SATURDAY" -> "토"
                            "SUNDAY" -> "일"
                            else -> hour.day
                        }

                        if (hour.isClosed) {
                            "$dayKr 휴무"
                        } else {
                            val breakText =
                                if (hour.breakFrom != null && hour.breakTo != null)
                                    " (브레이크 ${hour.breakFrom}~${hour.breakTo})"
                                else ""

                            "$dayKr ${hour.openFrom} ~ ${hour.openTo}$breakText"
                        }
                    }

                    _shopOpenHour.value = ShopOpenHourUiModel(
                        isOpen = todayOpen != null,
                        openTime = todayOpen?.openFrom ?: "",
                        closeTime = todayOpen?.openTo ?: "",
                        weeklyHours = weeklyTexts
                    )
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
                if (response.success) fetchShopIntro()
                else _errorMessage.value = response.message
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
                    shopId,
                    ShopBasicInfoRequest(name, phone, addressLine, detailAddress)
                )
                if (response.success) fetchShopDetail()
                else _errorMessage.value = response.message
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 정산 계좌 수정 */
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
                    shopId,
                    ShopAccountRequest(bankCode, accountNumber, accountHolder)
                )
                if (response.success) fetchShopAccount()
                else _errorMessage.value = response.message
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

/* ===================== 영업시간 UI 모델 ===================== */

data class ShopOpenHourUiModel(
    val isOpen: Boolean,
    val openTime: String,
    val closeTime: String,
    val weeklyHours: List<String>
)
