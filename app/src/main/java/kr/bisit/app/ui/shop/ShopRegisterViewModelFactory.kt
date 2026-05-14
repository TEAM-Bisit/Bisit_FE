package kr.bisit.app.ui.shop

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.bisit.app.data.repository.shop.ShopRegisterRepository

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
