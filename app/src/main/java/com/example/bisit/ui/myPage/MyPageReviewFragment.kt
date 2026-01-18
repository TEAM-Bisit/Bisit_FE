package com.example.bisit.ui.myPage

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.review.ReviewDetailItem
import com.example.bisit.data.model.review.ReviewListResponse
import com.example.bisit.data.model.review.ReviewRequest
import com.example.bisit.data.model.review.ReviewUpdateResponse
import com.example.bisit.databinding.FragmentMyPageReviewBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageReviewFragment : Fragment() {

    private var _binding: FragmentMyPageReviewBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: MyPageReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvReview.layoutManager = LinearLayoutManager(requireContext())
        
        // Load data
        fetchMyReviews()

        val backBtn = view.findViewById<ImageButton>(R.id.btn_back_review)
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun fetchMyReviews() {
        RetrofitClient.getReviewApi(requireContext()).getMyReviews(0, 10)
            .enqueue(object : Callback<ReviewListResponse> {
                override fun onResponse(
                    call: Call<ReviewListResponse>,
                    response: Response<ReviewListResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val reviewPage = response.body()?.data?.reviews
                        val items = reviewPage?.content ?: emptyList()
                        setupAdapter(items)
                    } else {
                         Toast.makeText(requireContext(), "리뷰 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReviewListResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupAdapter(items: List<ReviewDetailItem>) {
        adapter = MyPageReviewAdapter(items) { review ->
            showMoreDialog(review)
        }
        binding.rvReview.adapter = adapter
    }

    private fun showMoreDialog(review: ReviewDetailItem) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_my_page_review)

        dialog.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val tvEdit = dialog.findViewById<View>(R.id.tv_edit)
        val tvDelete = dialog.findViewById<View>(R.id.tv_delete)

        tvEdit.setOnClickListener {
            dialog.dismiss()
            showEditDialog(review)
        }

        tvDelete.setOnClickListener {
            dialog.dismiss()
            deleteReview(review.reviewId)
        }

        dialog.show()
    }

    private fun showEditDialog(review: ReviewDetailItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_my_page_review_edit, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val btnClose = dialogView.findViewById<View>(R.id.btnClose)
        val etReview = dialogView.findViewById<EditText>(R.id.etReview)
        val tvTextCount = dialogView.findViewById<TextView>(R.id.tvTextCount)
        val btnSummit = dialogView.findViewById<TextView>(R.id.btnSummit) // Changed to TextView if it is indeed a TextView with text

        val star1 = dialogView.findViewById<ImageView>(R.id.star1)
        val star2 = dialogView.findViewById<ImageView>(R.id.star2)
        val star3 = dialogView.findViewById<ImageView>(R.id.star3)
        val star4 = dialogView.findViewById<ImageView>(R.id.star4)
        val star5 = dialogView.findViewById<ImageView>(R.id.star5)

        // Init values
        etReview.setText(review.content)
        tvTextCount.text = "${review.content.length}/30자"
        
        var currentScore = review.rating // Use rating instead of score
        val stars = listOf(star1, star2, star3, star4, star5)

        fun updateSubmitButton(length: Int) {
            if (length > 0) {
                btnSummit.isEnabled = true
                btnSummit.setTextColor(Color.parseColor("#007AFF")) // Blue color for active
            } else {
                btnSummit.isEnabled = false
                btnSummit.setTextColor(Color.LTGRAY) // Gray color for inactive
            }
        }

        fun updateStars(score: Int) {
            currentScore = score
            stars.forEachIndexed { index, imageView ->
                if (index < score) {
                    imageView.alpha = 1.0f
                } else {
                    imageView.alpha = 0.3f
                }
            }
        }
        updateStars(currentScore)
        updateSubmitButton(review.content.length)

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { updateStars(index + 1) }
        }

        etReview.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textLength = s?.length ?: 0
                tvTextCount.text = "$textLength/30자"
                updateSubmitButton(textLength)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnSummit.setOnClickListener {
            val content = etReview.text.toString()
            val request = com.example.bisit.data.model.review.ReviewUpdateRequest(currentScore, content)

            RetrofitClient.getReviewApi(requireContext()).updateReview(review.reviewId, request)
                .enqueue(object : Callback<ReviewUpdateResponse> {
                    override fun onResponse(
                        call: Call<ReviewUpdateResponse>,
                        response: Response<ReviewUpdateResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "리뷰가 수정되었습니다.", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                            fetchMyReviews() // Refresh
                        } else {
                            Toast.makeText(requireContext(), "리뷰 수정 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ReviewUpdateResponse>, t: Throwable) {
                        Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun deleteReview(reviewId: Long) {
        RetrofitClient.getReviewApi(requireContext()).deleteReview(reviewId)
            .enqueue(object : Callback<ReviewUpdateResponse> {
                override fun onResponse(call: Call<ReviewUpdateResponse>, response: Response<ReviewUpdateResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "리뷰가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        // refresh the list
                        fetchMyReviews()
                    } else {
                        Toast.makeText(requireContext(), "리뷰 삭제 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ReviewUpdateResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
