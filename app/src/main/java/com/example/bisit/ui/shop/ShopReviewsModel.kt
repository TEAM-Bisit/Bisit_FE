package com.example.bisit.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.ReviewManageItem
import com.example.bisit.data.repository.shop.ReviewManageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopReviewsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reviewRepository = ReviewManageRepository(application)

    /* ===================== shopId ===================== */

    private val _shopId = MutableStateFlow<Long?>(null)

    fun setShopId(id: Long) {
        _shopId.value = id
    }

    private fun requireShopId(): Long {
        return _shopId.value
            ?: throw IllegalStateException("shopId가 설정되지 않았습니다")
    }

    /* ===================== 상태 ===================== */

    private val _reviews = MutableStateFlow<List<ReviewManageItem>>(emptyList())
    val reviews: StateFlow<List<ReviewManageItem>> = _reviews

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== GET ===================== */

    fun fetchReviews(
        page: Int = 0,
        size: Int = 10
    ) {
        viewModelScope.launch {
            val shopId = requireShopId()

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val pageResult = reviewRepository.fetchShopReviews(
                    shopId = shopId,
                    page = page,
                    size = size
                )
                _reviews.value = pageResult.content
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ===================== DELETE ===================== */

    fun deleteReview(reviewId: Long) {
        viewModelScope.launch {
            val shopId = requireShopId()

            _isLoading.value = true
            try {
                reviewRepository.deleteReview(
                    shopId = shopId,
                    reviewId = reviewId
                )

                // optimistic update (reviewId 기준)
                _reviews.value = _reviews.value.filterNot {
                    it.reviewId == reviewId
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
