package com.example.bisit.ui.myPage

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

class MyPageReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_page_review, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_coupons)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val dummyReviews = listOf(
            "매우 만족했습니다!",
            "서비스가 친절했어요.",
            "시설이 깔끔해서 좋았어요."
        )

        val adapter = MyPageReviewAdapter(dummyReviews) { clickedView ->
            showMoreDialog(clickedView)
        }
        recyclerView.adapter = adapter

        return view
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
}
