package com.example.bisit.ui.signUp

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.ShopIntroduceRequest
import com.example.bisit.data.model.shop.ShopIntroduceResponse
import com.example.bisit.data.model.shop.ShopPhotoResponse
import com.example.bisit.databinding.FragmentStoreIntroBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

// (어댑터 Import 필요)

class StoreIntroFragment : Fragment() {

    private var _binding: FragmentStoreIntroBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoAdapter: StorePhotoAdapter
    private val photoList = mutableListOf<Uri>()
    private val MAX_IMAGE_COUNT = 5

    private val signUpViewModel: SignUpViewModel by activityViewModels()

    private val pickMultipleMedia = registerForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGE_COUNT)
    ) { uris ->
        if (uris.isNotEmpty()) {
            // 기존 사진 + 새로 선택한 사진이 5장을 넘지 않게 처리
            val remainingSpace = MAX_IMAGE_COUNT - photoList.size
            if (remainingSpace > 0) {
                val addedPhotos = uris.take(remainingSpace)
                photoList.addAll(addedPhotos)
                photoAdapter.submitList(photoList.toList()) // 리스트 복사본 전달
                checkValidation()
            } else {
                Toast.makeText(requireContext(), "최대 5장까지만 등록 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStoreIntroBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(false)

        photoAdapter = StorePhotoAdapter(
            onAddClick = { openImagePicker() },
            onDeleteClick = { position -> removePhoto(position) }
        )

        binding.rvStoreImages.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = photoAdapter
        }

        binding.etStoreIntro.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                checkValidation()
            }
        })

        // 4. 서비스 선택 리스너
        binding.rgServiceType.setOnCheckedChangeListener { _, _ ->
            checkValidation()
        }
    }

    private fun checkValidation() {
        val introText = binding.etStoreIntro.text.toString()
        val isServiceSelected = binding.rgServiceType.checkedRadioButtonId != -1

        val isValid = introText.isNotBlank() && isServiceSelected && photoList.isNotEmpty()

        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(isValid)
    }

    private fun openImagePicker() {
        if (photoList.size >= MAX_IMAGE_COUNT) {
            Toast.makeText(requireContext(), "이미 사진이 가득 찼습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 갤러리 열기
        pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun removePhoto(position: Int) {
        photoList.removeAt(position)
        photoAdapter.submitList(photoList)
        checkValidation()
    }

    fun uploadDataAndNext(onSuccess: () -> Unit) {
        val shopId = signUpViewModel.shopId.value ?: 2
        val uploadedPhotoIds = mutableListOf<Long>()
        var uploadCount = 0

        // 사진이 아예 없는 경우 바로 소개글 등록으로 이동
        if (photoList.isEmpty()) {
            submitFinalIntroduce(shopId, emptyList(), onSuccess)
            return
        }

        // 1. 사진들부터 하나씩 업로드 (ShopApiService 사용)
        val storeApi = RetrofitClient.getStoreApi(requireContext())

        photoList.forEach { uri ->
            val file = uriToFile(uri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            storeApi.uploadPhoto(shopId, body).enqueue(object : Callback<ShopPhotoResponse> {
                override fun onResponse(call: Call<ShopPhotoResponse>, response: Response<ShopPhotoResponse>) {
                    if (response.isSuccessful) {
                        // 서버에서 준 photoId 추출
                        response.body()?.data?.photoId?.let { uploadedPhotoIds.add(it) }
                    }
                    checkAllPhotosUploaded()
                }

                override fun onFailure(call: Call<ShopPhotoResponse>, t: Throwable) {
                    checkAllPhotosUploaded()
                }

                private fun checkAllPhotosUploaded() {
                    uploadCount++
                    // 모든 사진 업로드 시도가 끝났으면 (성공 여부 상관없이 일단 진행)
                    if (uploadCount == photoList.size) {
                        submitFinalIntroduce(shopId, uploadedPhotoIds, onSuccess)
                    }
                }
            })
        }
    }

    private fun submitFinalIntroduce(shopId: Long, photoIds: List<Long>, onSuccess: () -> Unit) {
        val introText = binding.etStoreIntro.text.toString().trim()

        val serviceChannel = if (binding.rbShopService.isChecked) "SHOP" else "VISIT"

        val request = ShopIntroduceRequest(
            intro = introText,
            photoIds = photoIds,
            serviceChannel = serviceChannel
        )

        RetrofitClient.getStoreApi(requireContext()).updateIntroduce(shopId, request)
            .enqueue(object : Callback<ShopIntroduceResponse> {
                override fun onResponse(call: Call<ShopIntroduceResponse>, response: Response<ShopIntroduceResponse>) {
                    if (response.isSuccessful) {
                        onSuccess()
                    } else {
                        Toast.makeText(context, "매장 소개 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ShopIntroduceResponse>, t: Throwable) {
                    Toast.makeText(context, "네트워크 통신 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // URI -> File 변환 로직 (기존과 동일)
    private fun uriToFile(uri: Uri): File {
        val contentResolver = requireContext().contentResolver
        val file = File(requireContext().cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreIntroFragment()
    }
}