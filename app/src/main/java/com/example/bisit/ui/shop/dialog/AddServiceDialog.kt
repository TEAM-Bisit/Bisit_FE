package com.example.bisit.ui.shop.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.bisit.R
import com.example.bisit.databinding.DialogAddServiceBinding
import com.example.bisit.ui.shop.model.ServiceItem

class AddServiceDialog(
    private val prefill: ServiceItem? = null,
    private val onSaved: (ServiceItem) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddServiceBinding? = null
    private val binding get() = _binding!!

    /** 기존 데이터 스냅샷 */
    private var originItem: ServiceItem? = null
    private var originImage: Uri? = null

    /** 현재 선택된 이미지 (1장) */
    private var selectedImage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddServiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** 닫기 */
        binding.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }

        /** 초기 세팅 (수정 모드) */
        if (prefill != null) {
            originItem = prefill.copy()
            originImage = prefill.imageUri
            selectedImage = originImage

            selectedImage?.let {
                binding.ivImage.setImageURI(it)
                binding.ivImage.visibility = View.VISIBLE
                binding.ivAddImage.visibility = View.GONE
            }

            binding.etTitle.setText(prefill.title)
            binding.etPrice.setText(prefill.price.toString())
            binding.etDesc.setText(prefill.desc)

            val h = prefill.durationMin / 60
            val m = prefill.durationMin % 60
            binding.etHour.text = "%02d시간".format(h)
            binding.etMin.text = "%02d분".format(m)

            binding.btnAdd.text = getString(R.string.edit)
        } else {
            binding.etHour.text = "00시간"
            binding.etMin.text = "00분"
            binding.btnAdd.text = getString(R.string.add)
        }

        updateButtonState(false)

        /** 이미지 선택 */
        binding.layoutImage.setOnClickListener {
            openImagePicker()
        }

        /** 입력 감지 */
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateForm()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.etTitle.addTextChangedListener(watcher)
        binding.etPrice.addTextChangedListener(watcher)
        binding.etDesc.addTextChangedListener(watcher)

        binding.etHour.setOnClickListener { showDurationPicker() }
        binding.etMin.setOnClickListener { showDurationPicker() }

        /** 저장 */
        binding.btnAdd.setOnClickListener {
            if (!binding.btnAdd.isEnabled) return@setOnClickListener

            val item = (prefill ?: ServiceItem(0, "", "", 0)).copy(
                title = binding.etTitle.text.toString().trim(),
                price = binding.etPrice.text.toString().toInt(),
                desc = binding.etDesc.text.toString().trim(),
                durationMin = getHour() * 60 + getMinute(),
                imageUri = selectedImage
            )

            onSaved(item)
            dismissAllowingStateLoss()
        }
    }

    /** 이미지 피커 */
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data ?: return@registerForActivityResult
                selectedImage = uri

                binding.ivImage.setImageURI(uri)
                binding.ivImage.visibility = View.VISIBLE
                binding.ivAddImage.visibility = View.GONE

                validateForm()
            }
        }

    /** 유효성 검사 */
    private fun validateForm() {
        val title = binding.etTitle.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull()
        val h = getHour()
        val m = getMinute()

        val basicValid =
            title.isNotEmpty() &&
                    price != null &&
                    !(h == 0 && m == 0) &&
                    selectedImage != null

        val enabled = if (originItem == null) {
            basicValid
        } else {
            val changed =
                title != originItem!!.title ||
                        price != originItem!!.price ||
                        binding.etDesc.text.toString() != originItem!!.desc ||
                        (h * 60 + m) != originItem!!.durationMin ||
                        selectedImage != originImage

            basicValid && changed
        }

        updateButtonState(enabled)
    }

    /** 버튼 스타일 */
    private fun updateButtonState(enabled: Boolean) {
        binding.btnAdd.isEnabled = enabled
    }

    /** 시간 선택 */
    private fun showDurationPicker() {
        val context = requireContext()

        val hourPicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 12
            value = getHour()
            setFormatter { "%02d".format(it) }
        }

        val minutePicker = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            value = getMinute()
            setFormatter { "%02d".format(it) }
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 24, 32, 24)
            addView(hourPicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(minutePicker, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }

        AlertDialog.Builder(context)
            .setTitle("소요 시간 선택")
            .setView(layout)
            .setPositiveButton("확인") { _, _ ->
                binding.etHour.text = "%02d시간".format(hourPicker.value)
                binding.etMin.text = "%02d분".format(minutePicker.value)
                validateForm()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun getHour(): Int =
        binding.etHour.text.toString().replace("시간", "").toInt()

    private fun getMinute(): Int =
        binding.etMin.text.toString().replace("분", "").toInt()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val wm = requireActivity().windowManager.currentWindowMetrics
                wm.bounds.width()
            } else {
                resources.displayMetrics.widthPixels
            }

            setLayout((width * 0.806f).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
