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
        val tvServiceName: TextView = itemView.findViewById(R.id.tv_service_name)
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        val btnMore: ImageButton = itemView.findViewById(R.id.btn_more)
        val ivStar1: android.widget.ImageView = itemView.findViewById(R.id.iv_star1)
        val ivStar2: android.widget.ImageView = itemView.findViewById(R.id.iv_star2)
        val ivStar3: android.widget.ImageView = itemView.findViewById(R.id.iv_star3)
        val ivStar4: android.widget.ImageView = itemView.findViewById(R.id.iv_star4)
        val ivStar5: android.widget.ImageView = itemView.findViewById(R.id.iv_star5)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_page_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val item = reviews[position]
        holder.tvContent.text = item.content
        holder.tvServiceName.text = item.serviceName
        holder.tvDate.text = item.visitDate

        val stars = listOf(holder.ivStar1, holder.ivStar2, holder.ivStar3, holder.ivStar4, holder.ivStar5)
        val rating = item.rating
        stars.forEachIndexed { index, imageView ->
            if (index < rating) {
                imageView.alpha = 1.0f
            } else {
                imageView.alpha = 0.3f
            }
        }

        holder.btnMore.setOnClickListener {
            onMoreClick(item)
        }
    }

    override fun getItemCount() = reviews.size
}
