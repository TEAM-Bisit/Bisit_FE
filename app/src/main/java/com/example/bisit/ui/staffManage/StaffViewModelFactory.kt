package com.example.bisit.ui.staffManage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bisit.data.repository.staffManage.StaffManageRepository

class StaffManageViewModelFactory(
    private val repository: StaffManageRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StaffManageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StaffManageViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
