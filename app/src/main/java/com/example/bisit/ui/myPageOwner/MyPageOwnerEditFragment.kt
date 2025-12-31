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
import com.example.bisit.BuildConfig
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.api.SMSApiService
import com.example.bisit.databinding.FragmentMyPageOwnerEditBinding
import com.example.bisit.data.model.member.MyProfileResponse
import com.example.bisit.data.model.member.MemberUpdateRequest
import com.example.bisit.data.model.member.MemberUpdateResponse
import com.example.bisit.data.api.StaffManageApiService
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileOutputStream
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast
import android.widget.Button

class MyPageOwnerEditFragment : Fragment() {

    private var _binding: FragmentMyPageOwnerEditBinding? = null
    private val binding get() = _binding!!

    private var isPhoneVerified = false

    private val smsApi by lazy {
        Log.d(
            "SMS_DEBUG",
            "smsApi 초기화됨. BASE_SERVER_URL = ${BuildConfig.BASE_SERVER_URL}"
        )
        RetrofitClient.getSmsApi(requireContext())
    }

    private val staffApi by lazy {
        RetrofitClient.getStaffManageApi(requireContext())
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Log.d("OwnerEdit", "Selected URI: $uri")
            uploadImage(uri)
        } else {
            Log.d("OwnerEdit", "No media selected")
        }
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
        setupInputWatchers()

        fetchProfile()

        return binding.root
    }

    private fun fetchProfile() {
        RetrofitClient.getMemberApi(requireContext()).getMyProfile()
            .enqueue(object : Callback<MyProfileResponse> {
                override fun onResponse(call: Call<MyProfileResponse>, response: Response<MyProfileResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.let {
                            binding.etName.setText(it.name)
                            binding.etEmail.setText(it.email)
                            binding.etPhone.setText(it.phone)
                            
                            it.profileImage?.let { url ->
                                Glide.with(this@MyPageOwnerEditFragment)
                                    .load(url)
                                    .placeholder(R.drawable.img_mypage_owner)
                                    .into(binding.imgProfile)
                            }

                            // Initially disable btnBook as nothing has changed yet
                            binding.btnBook.isEnabled = false
                            binding.btnBook.backgroundTintList = resources.getColorStateList(R.color.gray, null)
                        }
                    } else {
                        Toast.makeText(requireContext(), "정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MyProfileResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uploadImage(uri: android.net.Uri) {
        val file = uriToFile(uri) ?: return
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

        staffApi.uploadProfileImage(body).enqueue(object : Callback<MyProfileResponse> {
            override fun onResponse(call: Call<MyProfileResponse>, response: Response<MyProfileResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "사진이 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                    response.body()?.data?.profileImage?.let { url ->
                        Glide.with(this@MyPageOwnerEditFragment).load(url).into(binding.imgProfile)
                    }
                } else {
                    Toast.makeText(requireContext(), "업로드 실패", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MyProfileResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uriToFile(uri: android.net.Uri): File? {
        return try {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "temp_profile_image.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e("OwnerEdit", "Error converting uri to file", e)
            null
        }
    }

    private fun setupInputWatchers() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkInputsChanged()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etEmail.addTextChangedListener(watcher)
    }

    private fun checkInputsChanged() {
        // Simple logic: if name or email is not empty, allow saving (phone is handled by isPhoneVerified)
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        
        // If phone is verified OR if phone hasn't been touched but name/email changed
        // For simplicity, let's enable save if name and email are valid format
        binding.btnBook.isEnabled = name.isNotEmpty() && email.contains("@")
        binding.btnBook.backgroundTintList = if (binding.btnBook.isEnabled) {
            resources.getColorStateList(R.color.blue_4076FF, null)
        } else {
            resources.getColorStateList(R.color.gray, null)
        }
    }

    private fun setupCameraDialog() {
        binding.icCamera.setOnClickListener {
            showEditDialog()
        }
    }

    private fun showEditDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_my_page_owner_edit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val btnClose = dialog.findViewById<Button>(R.id.btnClose)
        val btnWrite = dialog.findViewById<Button>(R.id.btnWrite)

        btnClose.setOnClickListener { dialog.dismiss() }
        btnWrite.setOnClickListener {
            pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupSaveDialog() {
        binding.btnBook.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val verificationCode = if (isPhoneVerified) binding.etPhone2.text.toString().trim() else null

        val request = MemberUpdateRequest(name, email, phone, verificationCode)

        RetrofitClient.getMemberApi(requireContext()).updateMyProfile(request)
            .enqueue(object : Callback<MemberUpdateResponse> {
                override fun onResponse(call: Call<MemberUpdateResponse>, response: Response<MemberUpdateResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "수정 실패"
                        Log.e("OwnerEdit", "Update Failed: $errorMsg")
                        Toast.makeText(requireContext(), "수정 실패: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MemberUpdateResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showDialog(layoutId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(layoutId)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.setCancelable(true)
        
        if (layoutId == R.layout.dialog_my_page_owner_edit_store) {
            val btnClose = dialog.findViewById<Button>(R.id.btnClose)
            val btnWrite = dialog.findViewById<Button>(R.id.btnWrite)
            btnClose?.setOnClickListener { dialog.dismiss() }
            btnWrite?.setOnClickListener { 
                updateProfile()
                dialog.dismiss() 
            }
        }

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
