package com.example.bisit.ui.customerMyReserve

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.example.bisit.R
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.review.ReviewRequest
import com.example.bisit.data.model.review.ReviewResponse
import com.example.bisit.databinding.DialogCustomerMyReserveReviewBinding
import com.example.bisit.databinding.FragmentCustomerMyReserveDetailBinding
import com.example.bisit.databinding.ItemCustomerMyReserveDetailHeaderBinding
import com.example.bisit.databinding.ItemCustomerMyReserveDetailBodyBinding
import com.example.bisit.databinding.ItemCustomerMyReserveDetailFootBinding
import com.example.bisit.data.model.reservation.ReservationDetailData
import com.example.bisit.data.model.reservation.ReservationDetailResponse
import com.example.bisit.databinding.DialogCancelReasonBinding
import com.example.bisit.data.model.reservation.CancelReservationRequest
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.Locale

class CustomerMyReserveDetailFragment : Fragment() {

    private var _binding: FragmentCustomerMyReserveDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var headerBinding: ItemCustomerMyReserveDetailHeaderBinding
    private lateinit var bodyBinding: ItemCustomerMyReserveDetailBodyBinding
    private lateinit var footBinding: ItemCustomerMyReserveDetailFootBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerMyReserveDetailBinding.inflate(inflater, container, false)

        headerBinding = ItemCustomerMyReserveDetailHeaderBinding.bind(binding.headerContainer.getChildAt(0))
        bodyBinding = ItemCustomerMyReserveDetailBodyBinding.bind(binding.bodyContainer.getChildAt(0))
        footBinding = ItemCustomerMyReserveDetailFootBinding.bind(binding.bodyContainer.findViewById(R.id.footerWrap))

        val reservationId = arguments?.getString("reservationId")
        if (reservationId != null) {
            fetchReservationDetail(reservationId.toLong())
        }

        setupButtons()

        return binding.root
    }

    private fun fetchReservationDetail(reservationId: Long) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = api.getReservationDetail(reservationId)
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        bindData(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindData(data: ReservationDetailData) {
        // Bind Header
        headerBinding.apply {
            tvReservNo.text = "예약 번호  ${data.orderId ?: data.reservationId}"
            tvShopName.text = data.shopName
            tvShopAddr.text = data.shopAddress
            
            val statusFromArg = arguments?.getString("status")
            val isCanceled = data.status.uppercase().contains("CANCEL") || statusFromArg == "취소"

            if (isCanceled) {
                tvStatus.text = "시술 취소"
                tvStatus.setTextColor(resources.getColor(R.color.main_red, null))
                btnReview.isEnabled = false
                btnReview.alpha = 0.5f // Visual feedback for disabled state
                btnReview.text = "리뷰 작성 불가"
                
                // Canceled UI adjustments
                binding.btnNextStep.visibility = View.GONE
                footBinding.root.visibility = View.VISIBLE
                footBinding.tvCancelReason.text = data.cancellationReason ?: ""
            } else {
                val currentStatus = data.status.uppercase()
                val isCompleted = currentStatus == "COMPLETED"
                val isConfirmed = currentStatus == "CUSTOMER_CONFIRMED"
                val isReviewed = data.isReviewed
                val canConfirm = data.canConfirm

                if (isCompleted) {
                    tvStatus.text = "시술 완료"
                    tvStatus.setTextColor(resources.getColor(R.color.blue_4076FF, null))
                } else if (isConfirmed) {
                    tvStatus.text = "고객 확정 완료"
                    tvStatus.setTextColor(resources.getColor(R.color.muted_gray, null))
                }

                // Review button logic
                // Only allow review if confirmed AND not already reviewed
                val canReview = isConfirmed && !isReviewed
                
                btnReview.isEnabled = canReview
                btnReview.alpha = if (canReview) 1.0f else 0.5f
                
                when {
                    isReviewed -> {
                        btnReview.text = "작성 완료"
                    }
                    isConfirmed -> {
                        btnReview.text = "리뷰 작성하기"
                    }
                    isCompleted -> {
                        btnReview.text = "시술 확정 후 작성 가능"
                    }
                    else -> {
                        btnReview.text = "리뷰 작성 불가"
                    }
                }
                
                btnReview.setOnClickListener {
                    if (canReview) {
                        showReviewDialog()
                    } else if (isReviewed) {
                        Toast.makeText(requireContext(), "이미 리뷰를 작성하셨습니다.", Toast.LENGTH_SHORT).show()
                    } else if (isCompleted && !isConfirmed) {
                        Toast.makeText(requireContext(), "시술 확정 후에 리뷰를 작성할 수 있습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                
                if (isCompleted && canConfirm) {
                    binding.buttonContainer.visibility = View.VISIBLE
                    binding.btnNextStep.visibility = View.VISIBLE
                    binding.btnNextStep.text = "확정하기"
                    binding.btnNextStep.setOnClickListener {
                        confirmReservation(data.reservationId)
                    }
                } else if (!isCompleted && !isCanceled && data.status.uppercase() != "CUSTOMER_CONFIRMED") {
                    // Scheduled reservation - show cancel button
                    binding.buttonContainer.visibility = View.VISIBLE
                    binding.btnNextStep.visibility = View.VISIBLE
                    binding.btnNextStep.text = "취소하기"
                    binding.btnNextStep.setOnClickListener {
                        showCancelDialog(data)
                    }
                } else {
                    binding.buttonContainer.visibility = View.GONE
                    binding.btnNextStep.visibility = View.GONE
                }
                footBinding.root.visibility = View.GONE
            }
        }

        // Bind Body
        bodyBinding.apply {
            tvValueDate.text = formatDateTime(data.reservedDate, data.startTime)
            tvValueTime.text = "${data.durationMin}분"
            tvValuePrice.text = "${NumberFormat.getNumberInstance(Locale.US).format(data.price)}원"
            tvValueName.text = data.customerName
        }

        // Show containers after binding is done
        binding.headerContainer.visibility = View.VISIBLE
        binding.bodyContainer.visibility = View.VISIBLE
    }

    private fun confirmReservation(reservationId: Long) {
        val dialog = Dialog(requireContext())
        val dialogBinding = com.example.bisit.databinding.DialogConfirmReservationBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnCancel.setOnClickListener { dialog.dismiss() }
        
        dialogBinding.btnConfirm.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.getReservationApi(requireContext())
                    val response = api.confirmReservation(reservationId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "시술이 확정되었습니다.", Toast.LENGTH_SHORT).show()
                        // Refresh data
                        fetchReservationDetail(reservationId)
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "시술 확정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        dialog.show()

        // 윈도우 크기를 넓게 설정 (화면 너비의 90%)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            val displayMetrics = resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.9).toInt()
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }
    private fun formatDateTime(date: String, time: String): String {

        return try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
            val dateObj = inputFormat.parse(date)
            val formattedDate = if (dateObj != null) outputFormat.format(dateObj) else date
            "$formattedDate  $time"
        } catch (e: Exception) {
            "$date  $time"
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }



    private fun showReviewDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogCustomerMyReserveReviewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            val window = dialog.window
            val params = window?.attributes
            val density = resources.displayMetrics.density

            params?.width = (324 * density).toInt()
            params?.height = (352 * density).toInt()
            window?.attributes = params
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialogBinding.etReview.addTextChangedListener {
            val textLength = it?.length ?: 0
            dialogBinding.tvTextCount.text = "$textLength/30자"
            
            // Enable button only if there is text
            val isValid = textLength > 0
            dialogBinding.btnSummit.isEnabled = isValid
            
            // Update background drawable
            if (isValid) {
                dialogBinding.btnSummit.setBackgroundResource(R.drawable.bg_dialog_button_enabled)
            } else {
                dialogBinding.btnSummit.setBackgroundResource(R.drawable.bg_dialog_button_disabled)
            }
            // Always keep text white as requested
            dialogBinding.btnSummit.setTextColor(resources.getColor(R.color.white, null))
        }
        
        // Initial state
        dialogBinding.btnSummit.isEnabled = false
        dialogBinding.btnSummit.setBackgroundResource(R.drawable.bg_dialog_button_disabled)
        dialogBinding.btnSummit.setTextColor(resources.getColor(R.color.white, null))

        // --- 별점 로직 시작 ---
        val stars = listOf(
            dialogBinding.star1, dialogBinding.star2, dialogBinding.star3,
            dialogBinding.star4, dialogBinding.star5
        )
        var currentScore = 5

        fun updateStars(score: Int) {
            currentScore = score
            stars.forEachIndexed { index, imageView ->
                if (index < score) {
                    imageView.alpha = 1.0f // 활성화
                } else {
                    imageView.alpha = 0.3f // 비활성화 (흐리게)
                }
            }
        }

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { updateStars(index + 1) }
        }
        // --- 별점 로직 끝 ---

        dialogBinding.btnSummit.setOnClickListener {
            submitReview(dialog, dialogBinding, currentScore)
        }

        dialog.show()
    }

    private fun submitReview(dialog: Dialog, dialogBinding: DialogCustomerMyReserveReviewBinding, score: Int) {
        val content = dialogBinding.etReview.text.toString()
        val reservationId = arguments?.getString("reservationId")?.toLongOrNull() ?: return

        val request = ReviewRequest(reservationId, score, content)

        RetrofitClient.getReviewApi(requireContext()).writeReview(request)
            .enqueue(object : Callback<ReviewResponse> {
                override fun onResponse(call: Call<ReviewResponse>, response: Response<ReviewResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        // Refresh reservation detail to update UI (disable review button)
                        fetchReservationDetail(reservationId)
                    } else {
                        Toast.makeText(requireContext(), "리뷰 등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showCancelDialog(data: ReservationDetailData) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogCancelReasonBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "예약 취소"
        dialogBinding.tvContentLabel.text = "${data.shopName} 예약을 취소하시겠습니까?"
        
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialogBinding.etReason.addTextChangedListener {
            val textLength = it?.length ?: 0
            dialogBinding.tvCharCount.text = "$textLength/50자"
            dialogBinding.btnSubmit.isEnabled = textLength > 0
            dialogBinding.btnSubmit.setTextColor(
                if (textLength > 0) resources.getColor(R.color.white, null) 
                else resources.getColor(R.color.muted_gray, null)
            )
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val reason = dialogBinding.etReason.text.toString()
            cancelReservation(data.reservationId, reason)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cancelReservation(reservationId: Long, reason: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val request = CancelReservationRequest(reason)
                val response = api.cancelReservation(reservationId, request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    fetchReservationDetail(reservationId)
                } else {
                    Toast.makeText(requireContext(), "예약 취소에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
