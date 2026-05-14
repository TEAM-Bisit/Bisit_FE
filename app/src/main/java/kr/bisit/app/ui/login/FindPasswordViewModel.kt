package kr.bisit.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindPasswordViewModel : ViewModel() {

    // 인증번호 입력 UI가 보이는지 여부
    private val _isVerificationUiVisible = MutableLiveData(false)
    val isVerificationUiVisible: LiveData<Boolean> get() = _isVerificationUiVisible

    // 휴대폰 인증이 최종 완료되었는지 여부
    private val _isPhoneVerified = MutableLiveData(false)
    val isPhoneVerified: LiveData<Boolean> get() = _isPhoneVerified

    val isVerificationUiVisibleInput: MutableLiveData<Boolean> = _isVerificationUiVisible
    val isPhoneVerifiedInput: MutableLiveData<Boolean> = _isPhoneVerified

    var resetToken: String? = null
}