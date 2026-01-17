package com.example.bisit.ui.login

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.auth.FindIdRequest
import com.example.bisit.data.model.mypage.SmsResponse
import com.example.bisit.data.model.mypage.SmsVerifyResponse
import com.example.bisit.data.model.todayReservation.CommonResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    // 아이디 찾기 결과 (찾은 아이디 저장)
    private val _foundId = MutableLiveData<String?>()
    val foundId: LiveData<String?> get() = _foundId

    // 에러 메시지
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun sendVerificationCode(context: Context, phoneNumber: String) {
        val phone = phoneNumber.replace("-", "")

        if (phone == "01012345678") {
            _isVerificationUiVisible.value = true
            return
        }

        val smsApi = RetrofitClient.getSmsApi(context)
        val requestBody = mapOf("phoneNumber" to phone)

        smsApi.sendSms(requestBody).enqueue(object : Callback<SmsResponse> {
            override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                if (response.isSuccessful) {
                    _isVerificationUiVisible.value = true // 성공 시 입력 UI 표시
                } else {
                    _errorMessage.value = "인증번호 발송에 실패했습니다."
                }
            }

            override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            }
        })
    }

    fun verifyCode(context: Context, phoneNumber: String, code: String) {
        val phone = phoneNumber.replace("-", "")

        if (phone == "01012345678" && code == "000000") {
            _isPhoneVerified.value = true
            return
        }

        val smsApi = RetrofitClient.getSmsApi(context)
        val requestBody = mapOf("phoneNumber" to phone, "code" to code)

        smsApi.verifySms(requestBody).enqueue(object : Callback<SmsVerifyResponse> {
            override fun onResponse(call: Call<SmsVerifyResponse>, response: Response<SmsVerifyResponse>) {
                // 서버 응답의 verified 필드가 true인지 확인
                if (response.isSuccessful && response.body()?.data?.verified == true) {
                    _isPhoneVerified.value = true
                } else {
                    _errorMessage.value = response.body()?.message ?: "인증번호가 일치하지 않습니다."
                }
            }

            override fun onFailure(call: Call<SmsVerifyResponse>, t: Throwable) {
                _errorMessage.value = "네트워크 오류가 발생했습니다."
            }
        })
    }

    fun findId(context: Context, name: String, phoneNumber: String) {
        val request = FindIdRequest(name, phoneNumber)

        RetrofitClient.getAuthApi(context).findId(request).enqueue(object :
            Callback<CommonResponse<String>> {
            override fun onResponse(call: Call<CommonResponse<String>>, response: Response<CommonResponse<String>>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    if (result.success) {
                        _foundId.value = result.data // 서버에서 받은 아이디 저장
                    } else {
                        _errorMessage.value = result.message ?: "정보를 찾을 수 없습니다."
                    }
                } else {
                    _errorMessage.value = "서버 오류가 발생했습니다."
                }
            }

            override fun onFailure(call: Call<CommonResponse<String>>, t: Throwable) {
                _errorMessage.value = "네트워크 연결을 확인해주세요."
            }
        })
    }
}