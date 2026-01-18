package com.example.bisit.ui.customerShop

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.shop.ReviewItem

class CustomerShopMoreReviewAdapter(
    private var reviews: List<ReviewItem>
) : RecyclerView.Adapter<CustomerShopMoreReviewAdapter.ReviewViewHolder>() {

    class ReviewViewHolder(val view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvContent: TextView = view.findViewById(R.id.tvReviewContent)
        val tvDate: TextView = view.findViewById(R.id.tvReviewDate)
        val tvReviewer: TextView = view.findViewById(R.id.tvReviewer)
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvManager: TextView = view.findViewById(R.id.tvManager)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shop_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val rev = reviews[position]
        
        holder.tvContent.text = rev.content
        holder.tvDate.text = "${rev.date} 방문"
        holder.tvReviewer.text = rev.author
        holder.tvServiceName.text = rev.serviceName ?: "기본 서비스"
        holder.tvManager.text = rev.staffName ?: "상점 원장님"

        // 별점 표시 (1개 이미지 + 숫자)
        holder.tvRating.text = String.format("%.1f", rev.rating.toDouble())
    }

    override fun getItemCount(): Int = reviews.size

    fun updateData(newReviews: List<ReviewItem>) {
        reviews = newReviews
        notifyDataSetChanged()
    }
}
