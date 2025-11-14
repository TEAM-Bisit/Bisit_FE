package com.example.bisit.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FindIdViewModel : ViewModel() {

    // 인증번호 입력 UI가 보이는지 여부
    private val _isVerificationUiVisible = MutableLiveData(false)
    val isVerificationUiVisible: LiveData<Boolean> get() = _isVerificationUiVisible

    // 휴대폰 인증이 최종 완료되었는지 여부
    private val _isPhoneVerified = MutableLiveData(false)
    val isPhoneVerified: LiveData<Boolean> get() = _isPhoneVerified

    // Fragment에서 상태를 변경할 수 있도록 public으로 노출
    // (또는 ViewModel에 'startVerification', 'completeVerification' 같은 함수를 만들어도 좋습니다)
    val isVerificationUiVisibleInput: MutableLiveData<Boolean> = _isVerificationUiVisible
    val isPhoneVerifiedInput: MutableLiveData<Boolean> = _isPhoneVerified
}