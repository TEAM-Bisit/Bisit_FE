package com.example.bisit.ui.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.TokenManager
import com.example.bisit.data.model.auth.LoginRequest
import com.example.bisit.data.model.auth.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun login(context: Context, id: String, pw: String) {
        val request = LoginRequest(id, pw)

        RetrofitClient.getAuthApi(context).login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    // success가 true이고 데이터가 존재할 때만 로그인 성공 처리
                    if (result.success && result.data != null) {
                        TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)
                        _loginResult.value = true
                    } else {
                        // 서버에서 success: false로 보낸 경우 (비번 불일치 등)
                        _errorMessage.value = result.message ?: "로그인에 실패했습니다."
                        _loginResult.value = false
                    }
                } else {
                    // HTTP 상태 코드가 200번대가 아닌 경우
                    _errorMessage.value = "서버 오류: ${response.code()}"
                    _loginResult.value = false
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginViewModel", "Network Error", t)
                _errorMessage.value = "네트워크 연결을 확인해주세요."
                _loginResult.value = false
            }
        })
    }
}