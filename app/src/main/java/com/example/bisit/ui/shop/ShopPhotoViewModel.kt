package com.example.bisit.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.ShopPhotoItem
import com.example.bisit.data.repository.photo.ShopPhotoRepository
import com.example.bisit.data.repository.shop.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopPhotoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val shopRepository = ShopRepository(application)
    private val photoRepository = ShopPhotoRepository(application)

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

    private val _photos = MutableStateFlow<List<ShopPhotoItem>>(emptyList())
    val photos: StateFlow<List<ShopPhotoItem>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== GET ===================== */

    fun fetchPhotos() {
        viewModelScope.launch {
            val shopId = requireShopId()

            _isLoading.value = true
            try {
                val response = shopRepository.getShopIntroduce(shopId)
                if (response.success) {
                    _photos.value = response.data.photos
                        .sortedBy { it.sortOrder }
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

    /* ===================== POST ===================== */

    fun uploadPhoto(file: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            val shopId = requireShopId()

            _isLoading.value = true
            try {
                val response = photoRepository.uploadShopPhoto(shopId, file)
                if (response.success) {
                    fetchPhotos()
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

    /* ===================== DELETE ===================== */

    fun deletePhoto(photoId: Long) {
        viewModelScope.launch {
            val shopId = requireShopId()

            _isLoading.value = true
            try {
                val response = photoRepository.deleteShopPhoto(shopId, photoId)
                if (response.success) {
                    fetchPhotos()
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
}
