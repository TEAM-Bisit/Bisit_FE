package kr.bisit.app.ui.shop.dialog

import android.app.Activity
import android.content.DialogInterface
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
import com.bumptech.glide.Glide
import kr.bisit.app.R
import kr.bisit.app.databinding.DialogAddServiceBinding
import kr.bisit.app.data.model.shop.TreatmentResponse

class AddServiceDialog(
    private val prefill: TreatmentResponse? = null,
    private val onSaved: (TreatmentResponse, Uri?) -> Unit,
    private val onClosed: (() -> Unit)? = null
) : DialogFragment() {

    private var _binding: DialogAddServiceBinding? = null
    private val binding get() = _binding!!

    /* ===================== 상태 ===================== */

    private var originItem: TreatmentResponse? = null
    private var selectedImageUri: Uri? = null

    /* ===================== Lifecycle ===================== */

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

        initClose()
        initPrefill()
        initInputWatcher()
        initClickListeners()
        updateButtonState(false)
    }

    // 어떤 방식으로 닫혀도( X / 추가하기 / 바깥 dismiss 등) 호출됨
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onClosed?.invoke()
    }

    /* ===================== 초기화 ===================== */

    private fun initClose() {
        binding.btnClose.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    private fun initPrefill() {
        if (prefill == null) {
            binding.etHour.text = "00시간"
            binding.etMin.text = "00분"
            binding.btnAdd.text = getString(R.string.add)
            return
        }

        originItem = prefill

        binding.etTitle.setText(prefill.name)
        binding.etPrice.setText(prefill.price.toString())
        binding.etDesc.setText(prefill.description)
        binding.etHour.text = "%02d시간".format(prefill.durationHours)
        binding.etMin.text = "%02d분".format(prefill.durationMinutes)

        prefill.photoUrl?.let { url ->
            binding.ivImage.visibility = View.VISIBLE
            binding.ivAddImage.visibility = View.GONE

            Glide.with(requireContext())
                .load(url)
                .into(binding.ivImage)
        }

        binding.btnAdd.text = getString(R.string.edit)
    }

    private fun initInputWatcher() {
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
    }

    private fun initClickListeners() {
        binding.layoutImage.setOnClickListener { openImagePicker() }
        binding.etHour.setOnClickListener { showDurationPicker() }
        binding.etMin.setOnClickListener { showDurationPicker() }

        binding.btnAdd.setOnClickListener {
            if (!binding.btnAdd.isEnabled) return@setOnClickListener

            val result = TreatmentResponse(
                treatmentId = prefill?.treatmentId ?: 0L,
                name = binding.etTitle.text.toString().trim(),
                description = binding.etDesc.text.toString().trim(),
                price = binding.etPrice.text.toString().toInt(),
                durationHours = getHour(),
                durationMinutes = getMinute(),
                photoUrl = prefill?.photoUrl,
                isActive = true
            )

            onSaved(result, selectedImageUri)
            dismissAllowingStateLoss()
        }
    }

    /* ===================== 이미지 선택 ===================== */

    private fun openImagePicker() {
        imagePickerLauncher.launch(
            Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        )
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult

            selectedImageUri = result.data?.data ?: return@registerForActivityResult

            binding.ivImage.visibility = View.VISIBLE
            binding.ivAddImage.visibility = View.GONE
            binding.ivImage.setImageURI(selectedImageUri)

            validateForm()
        }

    /* ===================== 검증 ===================== */

    private fun validateForm() {
        val name = binding.etTitle.text.toString().trim()
        val price = binding.etPrice.text.toString().toIntOrNull()
        val duration = getHour() * 60 + getMinute()

        val baseValid =
            name.isNotEmpty() &&
                    price != null &&
                    duration > 0

        val enabled =
            if (originItem == null) {
                baseValid
            } else {
                baseValid && (
                        name != originItem!!.name ||
                                price != originItem!!.price ||
                                binding.etDesc.text.toString() != originItem!!.description ||
                                getHour() != originItem!!.durationHours ||
                                getMinute() != originItem!!.durationMinutes ||
                                selectedImageUri != null
                        )
            }

        updateButtonState(enabled)
    }

    private fun updateButtonState(enabled: Boolean) {
        binding.btnAdd.isEnabled = enabled
    }

    /* ===================== 시간 선택 ===================== */

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

    /* ===================== Dialog 크기 ===================== */

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)

            val width = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().windowManager.currentWindowMetrics.bounds.width()
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