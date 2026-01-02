package com.example.bisit.ui.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.ReviewManageItem
import com.example.bisit.data.repository.shop.ReviewManageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopReviewsViewModel(
    context: Context
) : ViewModel() {

    private val repository = ReviewManageRepository(context)

    /* ===================== shopId ===================== */

    private var shopId: Long? = null

    fun initShop(shopId: Long) {
        if (this.shopId != null) return   // 중복 초기화 방지
        this.shopId = shopId
        loadReviews()
    }

    /* ===================== 리뷰 상태 ===================== */

    private val _reviews = MutableStateFlow<List<ReviewManageItem>>(emptyList())
    val reviews: StateFlow<List<ReviewManageItem>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== 리뷰 조회 ===================== */

    fun loadReviews(
        page: Int = 0,
        size: Int = 10
    ) {
        val id = shopId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            runCatching {
                repository.fetchShopReviews(id, page, size)
            }.onSuccess { pageResult ->
                _reviews.value = pageResult.content
            }.onFailure { e ->
                _errorMessage.value = e.message
            }.also {
                _isLoading.value = false
            }
        }
    }

    /* ===================== 리뷰 삭제 ===================== */

    fun deleteReview(reviewId: Long) {
        val id = shopId ?: return

        viewModelScope.launch {
            runCatching {
                repository.deleteReview(id, reviewId)
            }.onSuccess {
                // optimistic update
                _reviews.value = _reviews.value.filterNot {
                    it.hashCode() == reviewId.hashCode()
                }
            }.onFailure { e ->
                _errorMessage.value = e.message
            }
        }
    }
}
