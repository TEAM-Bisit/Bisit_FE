package com.example.bisit.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.model.shop.TreatmentRequest
import com.example.bisit.data.model.shop.TreatmentResponse
import com.example.bisit.data.repository.shop.ShopServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class ShopServiceViewModel(
    private val repository: ShopServiceRepository
) : ViewModel() {

    /* ===================== 상태 ===================== */

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /* ===================== 서비스 목록 ===================== */

    private val _treatments = MutableStateFlow<List<TreatmentResponse>>(emptyList())
    val treatments: StateFlow<List<TreatmentResponse>> = _treatments

    private val _hasNext = MutableStateFlow(false)
    val hasNext: StateFlow<Boolean> = _hasNext

    private var currentPage = 0

    /* ===================== 서비스 목록 조회 ===================== */

    fun loadTreatments(
        shopId: Long,
        isFirst: Boolean = false
    ) {
        viewModelScope.launch {
            if (isFirst) currentPage = 0

            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.getTreatments(
                    shopId = shopId,
                    page = currentPage,
                    size = 10
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val pageData = response.body()!!.data.treatments

                    _treatments.value =
                        if (isFirst) pageData.content
                        else _treatments.value + pageData.content

                    _hasNext.value = pageData.hasNext
                    currentPage++
                } else {
                    _errorMessage.value = response.body()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ===================== 서비스 생성 ===================== */

    fun createTreatment(
        shopId: Long,
        request: TreatmentRequest,
        photo: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.createTreatment(shopId, request, photo)

                if (response.isSuccessful && response.body()?.success == true) {
                    // 생성 후 목록 갱신
                    loadTreatments(shopId, isFirst = true)
                } else {
                    _errorMessage.value = response.body()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ===================== 서비스 수정 ===================== */

    fun updateTreatment(
        treatmentId: Long,
        shopId: Long,
        request: TreatmentRequest,
        photo: MultipartBody.Part?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response =
                    repository.updateTreatment(treatmentId, shopId, request, photo)

                if (response.isSuccessful && response.body()?.success == true) {
                    loadTreatments(shopId, isFirst = true)
                } else {
                    _errorMessage.value = response.body()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* ===================== 서비스 삭제 ===================== */

    fun deleteTreatment(
        treatmentId: Long,
        shopId: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.deleteTreatment(treatmentId, shopId)

                if (response.isSuccessful && response.body()?.success == true) {
                    loadTreatments(shopId, isFirst = true)
                } else {
                    _errorMessage.value = response.body()?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
