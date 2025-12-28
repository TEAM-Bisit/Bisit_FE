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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentStoreIntroBinding
// (어댑터 Import 필요)

class StoreIntroFragment : Fragment() {

    private var _binding: FragmentStoreIntroBinding? = null
    private val binding get() = _binding!!

    private lateinit var photoAdapter: StorePhotoAdapter
    private val photoList = mutableListOf<Uri>()
    private val MAX_IMAGE_COUNT = 5

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreIntroFragment()
    }
}