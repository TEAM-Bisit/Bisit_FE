package com.example.bisit.ui.myPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

import com.example.bisit.data.model.review.ReviewDetailItem

class MyPageReviewAdapter(
    private val reviews: List<ReviewDetailItem>,
    private val onMoreClick: (ReviewDetailItem) -> Unit
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
        val item = reviews[position]
        holder.tvContent.text = item.content
        holder.btnMore.setOnClickListener {
            onMoreClick(item)
        }
    }

    override fun getItemCount() = reviews.size
}
