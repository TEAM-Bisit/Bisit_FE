package com.example.bisit.ui.myPage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.databinding.FragmentMyPageReviewBinding

class MyPageReviewFragment : Fragment() {

    private var _binding: FragmentMyPageReviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dummyReviews = listOf(
            "매우 만족했습니다!",
            "서비스가 친절했어요.",
            "시설이 깔끔해서 좋았어요."
        )

        val adapter = MyPageReviewAdapter(dummyReviews) { clickedView ->
            showMoreDialog(clickedView)
        }

        binding.rvReview.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReview.adapter = adapter

        val backBtn = view.findViewById<ImageButton>(R.id.btn_back_review)
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showMoreDialog(view: View) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_my_page_review, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvEdit = dialogView.findViewById<View>(R.id.tv_edit)
        val tvDelete = dialogView.findViewById<View>(R.id.tv_delete)

        tvEdit.setOnClickListener {
            dialog.dismiss()
            showEditDialog()
        }

        tvDelete.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_my_page_review_edit, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val btnClose = dialogView.findViewById<View>(R.id.btnClose)
        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
