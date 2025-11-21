package com.example.bisit.ui.myPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.databinding.FragmentMyPageEditBinding
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.SMSApiService
import com.example.bisit.data.model.mypage.SmsResponse
import com.example.bisit.data.model.mypage.SmsVerifyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageEditFragment : Fragment() {

    private var _binding: FragmentMyPageEditBinding? = null
    private val binding get() = _binding!!

    private var isPhoneVerified = false

    private val smsApi by lazy {
        Log.d(
            "SMS_DEBUG",
            "smsApi 초기화됨. BASE_SERVER_URL = ${RetrofitClient.BASE_SERVER_URL}"
        )
        RetrofitClient.getSmsApi(requireContext())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etPhone.addTextChangedListener { text ->
            val phone = text.toString()
            Log.d("SMS_DEBUG", "전화번호 입력됨: $phone")
            binding.btnVerify.isEnabled = phone.length >= 10 && phone.startsWith("0")
        }

        binding.btnVerify.setOnClickListener {
            val phone = binding.etPhone.text.toString()

            Log.d("SMS_DEBUG", "번호 인증 버튼 클릭됨. 보내는 번호 = $phone")

            smsApi.sendSms(mapOf("phoneNumber" to phone))
                .enqueue(object : Callback<SmsResponse> {

                    override fun onResponse(
                        call: Call<SmsResponse>, response: Response<SmsResponse>
                    ) {
                        Log.d("SMS_DEBUG", "sendSms 응답 수신: code=${response.code()}, body=${response.body()}")

                        if (response.isSuccessful) {
                            Log.d("SMS_DEBUG", "sendSms 성공 상태")

                            if (response.body()?.success == true) {
                                Log.d("SMS_DEBUG", "SMS 발송 성공!")

                                binding.etPhone2.visibility = View.VISIBLE
                                binding.btnVerify2.visibility = View.VISIBLE

                                binding.btnVerify.text = "발송됨"
                                binding.btnVerify.isEnabled = false
                            } else {
                                Log.e("SMS_DEBUG", "SMS 발송 실패: success=false")
                            }
                        } else {
                            Log.e("SMS_DEBUG", "sendSms 응답 실패: ${response.errorBody()?.string()}")
                        }
                    }

                    override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                        Log.e("SMS_DEBUG", "sendSms 통신 오류: ${t.message}")
                    }
                })
        }

        binding.etPhone2.addTextChangedListener { text ->
            Log.d("SMS_DEBUG", "인증번호 입력: ${text.toString()}")
            binding.btnVerify2.isEnabled = !text.isNullOrEmpty()
        }

        binding.btnVerify2.setOnClickListener {

            val phone = binding.etPhone.text.toString()
            val code = binding.etPhone2.text.toString()

            Log.d("SMS_DEBUG", "인증번호 확인 클릭됨. phone=$phone, code=$code")

            smsApi.verifySms(
                mapOf(
                    "phoneNumber" to phone,
                    "code" to code
                )
            ).enqueue(object : Callback<SmsVerifyResponse> {

                override fun onResponse(
                    call: Call<SmsVerifyResponse>,
                    response: Response<SmsVerifyResponse>
                ) {
                    Log.d("SMS_DEBUG", "verifySms 응답: code=${response.code()}, body=${response.body()}")

                    if (response.isSuccessful && response.body()?.data?.verified == true) {

                        Log.d("SMS_DEBUG", "인증 성공!")

                        isPhoneVerified = true

                        binding.btnVerify2.text = "인증됨"
                        binding.btnVerify2.isEnabled = false
                        binding.etPhone2.isEnabled = false

                        binding.btnBook.isEnabled = true
                        binding.btnBook.backgroundTintList =
                            resources.getColorStateList(com.example.bisit.R.color.blue_4076FF, null)

                    } else {
                        Log.e("SMS_DEBUG", "인증 실패 또는 verified=false")
                    }
                }

                override fun onFailure(call: Call<SmsVerifyResponse>, t: Throwable) {
                    Log.e("SMS_DEBUG", "verifySms 통신 오류: ${t.message}")
                }
            })
        }

        binding.btnBook.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
