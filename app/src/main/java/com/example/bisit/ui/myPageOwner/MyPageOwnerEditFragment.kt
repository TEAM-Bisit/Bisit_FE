package com.example.bisit.ui.myPageOwner

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.SMSApiService
import com.example.bisit.databinding.FragmentMyPageOwnerEditBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageOwnerEditFragment : Fragment() {

    private var _binding: FragmentMyPageOwnerEditBinding? = null
    private val binding get() = _binding!!

    private var isPhoneVerified = false

    private val smsApi: SMSApiService by lazy {
        Log.d("SMS_DEBUG", "smsApi 초기화됨. BASE_SERVER_URL = ${RetrofitClient.BASE_SERVER_URL}")
        RetrofitClient.getSmsApi(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageOwnerEditBinding.inflate(inflater, container, false)

        binding.btnBook.isEnabled = false
        binding.btnBook.backgroundTintList =
            resources.getColorStateList(R.color.gray, null)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        setupCameraDialog()
        setupSaveDialog()
        setupPhoneInput()

        return binding.root
    }

    private fun setupCameraDialog() {
        binding.icCamera.setOnClickListener {
            showDialog(R.layout.dialog_my_page_owner_edit)
        }
    }

    private fun setupSaveDialog() {
        binding.btnBook.setOnClickListener {
            showDialog(R.layout.dialog_my_page_owner_edit_store)
        }
    }

    private fun showDialog(layoutId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutId)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        dialog.show()
    }

    private fun setupPhoneInput() {
        val phoneEt = binding.etPhone
        val verifyBtn = binding.btnVerify
        val codeEt = binding.etPhone2
        val completeBtn = binding.btnVerify2

        verifyBtn.isEnabled = false
        completeBtn.isEnabled = false

        phoneEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val phone = s.toString().trim()
                Log.d("SMS_DEBUG", "전화번호 입력됨: $phone")
                verifyBtn.isEnabled = phone.length >= 10 && phone.startsWith("0")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        verifyBtn.setOnClickListener {
            val phone = phoneEt.text.toString().trim()
            Log.d("SMS_DEBUG", "번호 인증 버튼 클릭됨. 보내는 번호 = $phone")
            sendSms(phone)
        }

        codeEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                completeBtn.isEnabled = !s.isNullOrEmpty()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        completeBtn.setOnClickListener {
            val phone = phoneEt.text.toString().trim()
            val code = codeEt.text.toString().trim()
            Log.d("SMS_DEBUG", "인증번호 확인 클릭됨. phone=$phone, code=$code")
            verifySms(phone, code)
        }
    }

    private fun sendSms(phone: String) {

        Log.d("SMS_DEBUG", "SMS 발송 요청: $phone")

        smsApi.sendSms(mapOf("phoneNumber" to phone))
            .enqueue(object : Callback<com.example.bisit.data.model.mypage.SmsResponse> {

                override fun onResponse(
                    call: Call<com.example.bisit.data.model.mypage.SmsResponse>,
                    response: Response<com.example.bisit.data.model.mypage.SmsResponse>
                ) {
                    Log.d(
                        "SMS_DEBUG",
                        "sendSms 응답 수신: code=${response.code()}, body=${response.body()}"
                    )

                    if (response.isSuccessful && response.body()?.success == true) {

                        Log.d("SMS_DEBUG", "SMS 발송 성공!")

                        binding.etPhone2.visibility = View.VISIBLE
                        binding.btnVerify2.visibility = View.VISIBLE

                        binding.etPhone.isEnabled = false
                        binding.btnVerify.text = "발송됨"
                        binding.btnVerify.isEnabled = false

                    } else {
                        Log.e("SMS_DEBUG", "SMS 발송 실패: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(
                    call: Call<com.example.bisit.data.model.mypage.SmsResponse>,
                    t: Throwable
                ) {
                    Log.e("SMS_DEBUG", "sendSms 통신 오류: ${t.message}")
                }
            })
    }

    private fun verifySms(phone: String, code: String) {

        smsApi.verifySms(
            mapOf(
                "phoneNumber" to phone,
                "code" to code
            )
        ).enqueue(object :
            Callback<com.example.bisit.data.model.mypage.SmsVerifyResponse> {

            override fun onResponse(
                call: Call<com.example.bisit.data.model.mypage.SmsVerifyResponse>,
                response: Response<com.example.bisit.data.model.mypage.SmsVerifyResponse>
            ) {

                Log.d(
                    "SMS_DEBUG",
                    "verifySms 응답: code=${response.code()}, body=${response.body()}"
                )

                if (response.isSuccessful && response.body()?.data?.verified == true) {

                    Log.d("SMS_DEBUG", "인증 성공!")

                    isPhoneVerified = true

                    binding.btnVerify2.text = "인증됨"
                    binding.btnVerify2.isEnabled = false
                    binding.etPhone2.isEnabled = false

                    binding.btnBook.isEnabled = true
                    binding.btnBook.backgroundTintList =
                        resources.getColorStateList(R.color.blue_4076FF, null)

                } else {
                    Log.e("SMS_DEBUG", "인증 실패 또는 verified=false")
                }
            }

            override fun onFailure(
                call: Call<com.example.bisit.data.model.mypage.SmsVerifyResponse>,
                t: Throwable
            ) {
                Log.e("SMS_DEBUG", "verifySms 통신 오류: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
