package com.example.bisit.ui.myPage

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
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

        val adapter = MyPageReviewAdapter(dummyReviews) { position ->
            showMoreDialog(position)
        }

        binding.rvReview.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReview.adapter = adapter

        val backBtn = view.findViewById<ImageButton>(R.id.btn_back_review)
        backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showMoreDialog(position: Int) {
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

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
