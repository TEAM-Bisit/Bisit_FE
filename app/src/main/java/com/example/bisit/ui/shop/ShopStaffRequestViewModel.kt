package com.example.bisit.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bisit.data.repository.staffManage.StaffManageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopStaffRequestViewModel(
    private val repository: StaffManageRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StaffRequestState())
    val state: StateFlow<StaffRequestState> = _state.asStateFlow()

    /**
     * Shop 진입 시 호출
     * → 새 직원 신청 존재 여부만 확인
     */
    fun checkPendingStaffExists(shopId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            runCatching {
                repository.hasPendingStaff(shopId)
            }.onSuccess { exists ->
                _state.value = _state.value.copy(
                    hasPendingRequest = exists,
                    isLoading = false
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    hasPendingRequest = false,
                    isLoading = false
                )
            }
        }
    }

    /**
     * StaffRequestsFragment에서 승인/거절 후 즉시 반영
     */
    fun updatePendingStaffState(hasPending: Boolean) {
        _state.value = _state.value.copy(
            hasPendingRequest = hasPending
        )
    }
}
