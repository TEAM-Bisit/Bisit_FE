package kr.bisit.app.ui.myPage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.bisit.app.BuildConfig
import kr.bisit.app.databinding.FragmentMyPageEditBinding
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.mypage.SmsResponse
import kr.bisit.app.data.model.mypage.SmsVerifyResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import kr.bisit.app.data.model.member.MyProfileResponse

class MyPageEditFragment : Fragment() {

    private var _binding: FragmentMyPageEditBinding? = null
    private val binding get() = _binding!!

    private var isPhoneVerified = false

    private val smsApi by lazy {
        Log.d(
            "SMS_DEBUG",
            "smsApi 초기화됨. BASE_SERVER_URL = ${BuildConfig.BASE_SERVER_URL}"
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

        // Fetch Profile Info
        fetchMyProfile()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.etPhone.addTextChangedListener { text ->
            val phone = text.toString()
            Log.d("SMS_DEBUG", "전화번호 입력됨: $phone")
            binding.btnVerify.isEnabled = phone.length >= 10 && phone.startsWith("0")
            
            // Reset verification state and disable save button if phone changes
            isPhoneVerified = false
            binding.btnBook.isEnabled = false
            binding.btnBook.backgroundTintList =
                resources.getColorStateList(kr.bisit.app.R.color.outline_gray, null)
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
                            resources.getColorStateList(kr.bisit.app.R.color.blue_4076FF, null)
                    } else {
                        Log.e("SMS_DEBUG", "인증 실패 또는 verified=false")
                    }
                }

                override fun onFailure(call: Call<SmsVerifyResponse>, t: Throwable) {
                    Log.e("SMS_DEBUG", "verifySms 통신 오류: ${t.message}")
                }
            })
        }

        // Initial logic setup
        setupUpdateButton()
        // Default disabled until loaded or edited? existing code had false. 
        // We enable it in fetch success, or we can enable it here if you want edits immediately. 
        // Logic in fetchMyProfile enables it after load.
        binding.btnBook.isEnabled = false 
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun fetchMyProfile() {
        RetrofitClient.getMemberApi(requireContext()).getMyProfile()
            .enqueue(object : Callback<MyProfileResponse> {
                override fun onResponse(
                    call: Call<MyProfileResponse>,
                    response: Response<MyProfileResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val data = response.body()?.data
                        binding.etName.setText(data?.name)
                        binding.etEmail.setText(data?.email)
                        if (!data?.phone.isNullOrEmpty()) {
                            binding.etPhone.setText(data?.phone)
                        }
                        
                        // Keep Save button disabled until verified/re-verified
                        binding.btnBook.isEnabled = false
                        binding.btnBook.backgroundTintList =
                            resources.getColorStateList(kr.bisit.app.R.color.outline_gray, null)
                    }
                }

                override fun onFailure(call: Call<MyProfileResponse>, t: Throwable) {
                    Log.e("MyPageEditFragment", "Fetch profile failed", t)
                }
            })
    }
    
    private fun setupUpdateButton() {
        binding.btnBook.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val code = binding.etPhone2.text.toString().trim().ifEmpty { null }
        
        // Basic validation
        if (name.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "이름을 입력해주세요.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) { // Add email regex validation if needed
             android.widget.Toast.makeText(requireContext(), "이메일을 입력해주세요.", android.widget.Toast.LENGTH_SHORT).show()
             return
        }
        
        // If phone changed, code must be verified (isPhoneVerified flag handles local check, backend also checks)
        // If phone is different from initial, code is required. 
        // For now, simplistically pass what we have. API requirement: "Verification code required when phone changes"
        
        val request = kr.bisit.app.data.model.member.MemberUpdateRequest(
            name = name,
            email = email,
            phone = phone,
            verificationCode = code
        )

        binding.btnBook.isEnabled = false
        
        RetrofitClient.getMemberApi(requireContext()).updateMyProfile(request)
            .enqueue(object : Callback<kr.bisit.app.data.model.member.MemberUpdateResponse> {
                override fun onResponse(
                    call: Call<kr.bisit.app.data.model.member.MemberUpdateResponse>,
                    response: Response<kr.bisit.app.data.model.member.MemberUpdateResponse>
                ) {
                     binding.btnBook.isEnabled = true
                    if (response.isSuccessful && response.body()?.success == true) {
                        android.widget.Toast.makeText(requireContext(), "정보가 수정되었습니다.", android.widget.Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        val msg = response.body()?.message ?: "수정에 실패했습니다."
                        android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<kr.bisit.app.data.model.member.MemberUpdateResponse>, t: Throwable) {
                    binding.btnBook.isEnabled = true
                    android.widget.Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }
}