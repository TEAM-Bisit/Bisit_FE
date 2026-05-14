package kr.bisit.app.ui.myPageOwner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.bisit.app.data.model.coupon.CreateCouponRequest
import kr.bisit.app.data.model.coupon.OwnerCouponItem
import kr.bisit.app.data.model.coupon.UpdateCouponRequest
import kr.bisit.app.data.repository.coupon.OwnerCouponRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OwnerCouponViewModel(private val repository: OwnerCouponRepository) : ViewModel() {

    private val _coupons = MutableStateFlow<List<OwnerCouponItem>>(emptyList())
    val coupons: StateFlow<List<OwnerCouponItem>> = _coupons

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _operationSuccess = MutableStateFlow<Boolean>(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess

    fun fetchCoupons(shopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getShopCoupons(shopId)
                _coupons.value = response.data.coupons.content
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCoupon(shopId: Long, request: CreateCouponRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                repository.createCoupon(shopId, request)
                _operationSuccess.value = true
                fetchCoupons(shopId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCoupon(couponId: Long, shopId: Long, request: UpdateCouponRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                repository.updateCoupon(couponId, shopId, request)
                _operationSuccess.value = true
                fetchCoupons(shopId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCoupon(couponId: Long, shopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            try {
                repository.deleteCoupon(couponId, shopId)
                _operationSuccess.value = true
                fetchCoupons(shopId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetOperationSuccess() {
        _operationSuccess.value = false
    }
}
