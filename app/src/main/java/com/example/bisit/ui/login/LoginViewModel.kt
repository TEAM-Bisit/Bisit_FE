package com.example.bisit.ui.login

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.TokenManager
import com.example.bisit.data.model.auth.LoginRequest
import com.example.bisit.data.model.auth.LoginResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {

    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> get() = _loginResult

    private val _userType = MutableLiveData<String>()
    val userType: LiveData<String> get() = _userType

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    private val _errorCode = MutableLiveData<String?>()
    val errorCode: LiveData<String?> get() = _errorCode

    fun login(context: Context, id: String, pw: String) {
        val request = LoginRequest(id, pw)

        RetrofitClient.getAuthApi(context).login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!

                    if (result.success && result.data != null) {
                        TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)

                        val role = getRoleFromToken(result.data.accessToken)

                        _userType.value = when (role) {
                            "OWNER" -> "owner"
                            "CUSTOMER" -> "customer"
                            else -> "none"
                        }

                        _loginResult.value = true
                    } else {
                        parseError(response.errorBody()?.string())
                        _loginResult.value = false
                    }
                } else {
                    parseError(response.errorBody()?.string())
                    _loginResult.value = false
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginViewModel", "Network Error", t)
                _errorCode.value = "NETWORK_ERROR"
                _errorMessage.value = "네트워크 연결을 확인해주세요."
                _loginResult.value = false
            }
        })
    }

    private fun getRoleFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size < 2) return null

            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)

            val jsonObject = JSONObject(decodedString)

            if (!jsonObject.has("role") || jsonObject.isNull("role")) {
                null
            } else {
                jsonObject.getString("role")
            }
        } catch (e: Exception) {
            Log.e("LoginViewModel", "Token decoding error", e)
            null
        }
    }

    private fun parseError(errorBodyString: String?) {
        try {
            val json = JSONObject(errorBodyString ?: "{}")
            _errorCode.value = json.optString("code", "UNKNOWN")
            _errorMessage.value = json.optString("message", "오류가 발생했습니다.")
        } catch (e: Exception) {
            _errorCode.value = "UNKNOWN"
            _errorMessage.value = "로그인 요청 중 오류가 발생했습니다."
        }
    }
}