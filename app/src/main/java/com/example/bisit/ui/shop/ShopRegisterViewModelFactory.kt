package com.example.bisit.ui.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bisit.data.repository.shop.ShopRegisterRepository

class ShopRegisterViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ShopRegisterViewModel(
            ShopRegisterRepository(context)
        ) as T
    }
}
