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
import com.example.bisit.data.model.member.MyPageResponse
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
                        // 1. 토큰 저장
                        TokenManager.saveTokens(context, result.data.accessToken, result.data.refreshToken)

                        // 2. 토큰에서 Role 추출하여 사용자 타입(owner/customer) 설정
                        val role = getRoleFromToken(result.data.accessToken)
                        _userType.value = when (role) {
                            "OWNER" -> "owner"
                            "CUSTOMER" -> "customer"
                            else -> "none"
                        }

                        // 3. 즉시 마이페이지 정보를 조회하여 memberId 가져오기 (성공 시 여기서 _loginResult를 true로 바꿈)
                        fetchMemberIdAndFinish(context)
                    } else {
                        // 서버 응답은 왔으나 success가 false인 경우
                        _errorMessage.value = result.message ?: "로그인에 실패했습니다."
                        _loginResult.value = false
                    }
                } else {
                    // HTTP 상태 코드가 200번대가 아닌 경우 (에러 바디 파싱)
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

    // 마이페이지 조회를 통해 memberId를 확보한 후 로그인을 완료하는 함수
    private fun fetchMemberIdAndFinish(context: Context) {
        RetrofitClient.getMemberApi(context).getMyPage().enqueue(object : Callback<MyPageResponse> {
            override fun onResponse(call: Call<MyPageResponse>, response: Response<MyPageResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val memberId = response.body()?.data?.memberId ?: -1L

                    // 3. memberId 저장
                    TokenManager.saveMemberId(context, memberId)

                    // 4. 최종 로그인 성공 처리 (모든 정보가 다 저장된 후)
                    _loginResult.value = true
                } else {
                    _errorMessage.value = "사용자 정보를 가져오는 데 실패했습니다."
                    _loginResult.value = false
                }
            }

            override fun onFailure(call: Call<MyPageResponse>, t: Throwable) {
                _errorMessage.value = "사용자 정보 조회 중 네트워크 오류가 발생했습니다."
                _loginResult.value = false
            }
        })
    }

    // JWT 토큰에서 Role 정보를 읽어오는 헬퍼 함수
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

    // 서버 에러 응답(JSON)에서 code와 message를 추출하는 함수
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