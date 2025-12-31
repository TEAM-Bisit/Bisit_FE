package com.example.bisit.ui.signUp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {
    val isTermServiceChecked = MutableLiveData(false)
    val isTermLocationChecked = MutableLiveData(false)
    val isAllChecked = MutableLiveData(false)
    var shouldShowTermsSheetOnReturn = false

    val isVerificationUiVisible = MutableLiveData(false)
    val isPhoneVerified = MutableLiveData(false)

    var name: String = ""
    var email: String = ""
    var phone: String = ""
    var gender: String = "MALE"
}