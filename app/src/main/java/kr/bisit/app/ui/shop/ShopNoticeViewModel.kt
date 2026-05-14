package kr.bisit.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.bisit.app.data.model.shop.ShopNoticeResponse
import kr.bisit.app.data.repository.shop.ShopNoticeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShopNoticeViewModel(
    private val repository: ShopNoticeRepository
) : ViewModel() {

    private val _notices = MutableStateFlow<List<ShopNoticeResponse>>(emptyList())
    val notices: StateFlow<List<ShopNoticeResponse>> = _notices

    private val _sortType = MutableStateFlow("recent") // recent | oldest
    val sortType: StateFlow<String> = _sortType

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /** ================= 조회 ================= */
    fun loadNotices(shopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val sortOrder = if (_sortType.value == "recent") "desc" else "asc"

            try {
                val response = repository.getNotices(shopId, sortOrder)
                if (response?.success == true) {
                    _notices.value = response.data.notices
                } else {
                    _errorMessage.value = response?.message
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** ================= 정렬 ================= */
    fun changeSort(shopId: Long, sort: String) {
        _sortType.value = sort
        loadNotices(shopId)
    }

    /** ================= 삭제 ================= */
    fun deleteNotice(shopId: Long, noticeId: Long) {
        viewModelScope.launch {
            repository.deleteNotice(shopId, noticeId)
            loadNotices(shopId)
        }
    }

    /** ================= 추가 ================= */
    fun createNotice(
        shopId: Long,
        title: String,
        content: String
    ) {
        viewModelScope.launch {
            repository.createNotice(shopId, title, content)
            loadNotices(shopId)
        }
    }

    /** ================= 수정 ================= */
    fun updateNotice(
        shopId: Long,
        noticeId: Long,
        title: String,
        content: String
    ) {
        viewModelScope.launch {
            repository.updateNotice(shopId, noticeId, title, content)
            loadNotices(shopId)
        }
    }
}
