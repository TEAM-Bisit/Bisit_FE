package kr.bisit.app.ui.myPageOwner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.repository.coupon.OwnerCouponRepository

class OwnerCouponViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OwnerCouponViewModel::class.java)) {
            val apiService = RetrofitClient.getCouponApi(context)
            val repository = OwnerCouponRepository(apiService)
            return OwnerCouponViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
