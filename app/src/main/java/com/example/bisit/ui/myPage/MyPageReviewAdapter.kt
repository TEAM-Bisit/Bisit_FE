package com.example.bisit.ui.myPage

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

class MyPageReviewAdapter(
    private val reviews: List<String>,
    private val onMoreClick: (View) -> Unit
) : RecyclerView.Adapter<MyPageReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val btnMore: ImageButton = itemView.findViewById(R.id.btn_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_page_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.tvContent.text = reviews[position]
        holder.btnMore.setOnClickListener {
            onMoreClick(it)
        }
    }

    override fun getItemCount() = reviews.size
}
