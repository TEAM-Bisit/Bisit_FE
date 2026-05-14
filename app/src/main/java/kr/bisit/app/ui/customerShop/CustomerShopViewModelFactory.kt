package kr.bisit.app.ui.customerShop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.bisit.app.data.repository.customerShop.CustomerShopRepository

class CustomerShopViewModelFactory(
    private val repo: CustomerShopRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerShopViewModel(repo) as T
    }
}