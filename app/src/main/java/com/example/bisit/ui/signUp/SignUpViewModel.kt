package com.example.bisit.ui.signUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {
    val isTermServiceChecked = MutableLiveData(false)
    val isTermLocationChecked = MutableLiveData(false)
    val isAllChecked = MutableLiveData(false)
    var shouldShowTermsSheetOnReturn = false

    val isVerificationUiVisible = MutableLiveData(false)
    val isPhoneVerified = MutableLiveData(false)

    val isEmailAvailable = MutableLiveData(false)

    var name: String = ""
    var email: String = ""
    var phone: String = ""
    var gender: String = "MALE"

    private val _businessRegNo = MutableLiveData<String>()
    val businessRegNo: LiveData<String> = _businessRegNo

    private val _shopId = MutableLiveData<Long>()
    val shopId: LiveData<Long> = _shopId

    fun setBusinessRegNo(no: String) {
        _businessRegNo.value = no
    }

    fun setShopId(id: Long) {
        _shopId.value = id
    }
}