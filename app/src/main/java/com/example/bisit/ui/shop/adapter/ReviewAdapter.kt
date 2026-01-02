package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.shop.ReviewManageItem
import com.example.bisit.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val onMoreClick: (ReviewManageItem) -> Unit
) : ListAdapter<ReviewManageItem, ReviewAdapter.VH>(DIFF) {

    companion object DIFF : DiffUtil.ItemCallback<ReviewManageItem>() {
        override fun areItemsTheSame(
            oldItem: ReviewManageItem,
            newItem: ReviewManageItem
        ): Boolean {
            // reviewId가 있다면 그걸로 교체 권장
            return oldItem.createdAt == newItem.createdAt
        }

        override fun areContentsTheSame(
            oldItem: ReviewManageItem,
            newItem: ReviewManageItem
        ): Boolean = oldItem == newItem
    }

    inner class VH(val b: ItemReviewBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.b.apply {

            // 날짜 (API 24 대응)
            val dateString = item.visitDate.toString()
            val date = java.sql.Date.valueOf(dateString)
            val formatter = SimpleDateFormat("yyyy.MM.dd, E", Locale.KOREAN)
            tvVisitDate.text = formatter.format(date) + " 방문"

            tvServiceName.text = item.serviceName
            tvCustomerName.text = item.reviewerName
            tvReviewContent.text = item.content

            // 별점 처리
            val stars = listOf(star1, star2, star3, star4, star5)
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating)
                        R.drawable.ic_star_filled
                    else
                        R.drawable.ic_star_empty
                )
            }

            btnMore.setOnClickListener {
                onMoreClick(item)
            }
        }
    }
}
