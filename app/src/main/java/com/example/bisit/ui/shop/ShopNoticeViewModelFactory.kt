package com.example.bisit.ui.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bisit.data.repository.shop.ShopNoticeRepository

class ShopNoticeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShopNoticeViewModel::class.java)) {
            val repository = ShopNoticeRepository(context.applicationContext)
            return ShopNoticeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
