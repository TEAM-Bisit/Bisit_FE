package kr.bisit.app.ui.shop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.bisit.app.data.repository.staffManage.StaffManageRepository

class ShopStaffRequestViewModelFactory(
    private val repository: StaffManageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopStaffRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShopStaffRequestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
