package kr.bisit.app.ui.staffManage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kr.bisit.app.data.model.staffManage.ApprovedStaffItem
import kr.bisit.app.data.model.staffManage.PendingStaffItem
import kr.bisit.app.data.repository.staffManage.StaffManageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StaffManageViewModel(
    private val repository: StaffManageRepository
) : ViewModel() {

    // 승인 대기 직원 요청
    private val _pendingStaffs =
        MutableStateFlow<List<PendingStaffItem>>(emptyList())
    val pendingStaffs: StateFlow<List<PendingStaffItem>> = _pendingStaffs

    // 승인된 직원 목록
    private val _approvedStaffs = MutableStateFlow<List<ApprovedStaffItem>>(emptyList())
    val approvedStaffs: StateFlow<List<ApprovedStaffItem>> = _approvedStaffs


    fun loadPendingStaffs(shopId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.getPendingStaffs(shopId)
            }.onSuccess { list ->
                _pendingStaffs.value = list
            }
        }
    }

    fun approveStaff(shopId: Long, staffId: Long) {
        viewModelScope.launch {
            repository.approveStaff(shopId, staffId)
            loadPendingStaffs(shopId) // 승인 후 목록 갱신
        }
    }

    fun rejectStaff(shopId: Long, staffId: Long) {
        viewModelScope.launch {
            repository.rejectStaff(shopId, staffId)
            loadPendingStaffs(shopId) // 거절 후 목록 갱신
        }
    }

    fun loadApprovedStaffs(shopId: Long) {
        viewModelScope.launch {
            val list = repository.getApprovedStaffs(shopId)
            _approvedStaffs.value = list // 승인된 직원 목록
        }
    }

    fun deleteStaff(shopId: Long, staffId: Long) {
        viewModelScope.launch {
            repository.deleteStaff(shopId, staffId)
            // 현재 리스트에서 제거
            _approvedStaffs.value =
                _approvedStaffs.value.filterNot { it.staffId == staffId }
        }
    }
}
