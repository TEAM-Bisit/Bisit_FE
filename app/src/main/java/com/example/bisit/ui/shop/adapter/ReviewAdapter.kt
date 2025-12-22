package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemReviewBinding
import com.example.bisit.data.model.review.ReviewDetailItem

class ReviewAdapter(private val onMoreClick: (ReviewDetailItem) -> Unit) :
    ListAdapter<ReviewDetailItem, ReviewAdapter.VH>(DIFF) {


    companion object DIFF : DiffUtil.ItemCallback<ReviewDetailItem>() {
        override fun areItemsTheSame(oldItem: ReviewDetailItem, newItem: ReviewDetailItem) = oldItem.reviewId == newItem.reviewId
        override fun areContentsTheSame(oldItem: ReviewDetailItem, newItem: ReviewDetailItem) = oldItem == newItem
    }


    inner class VH(val b: ItemReviewBinding) : RecyclerView.ViewHolder(b.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.apply {
            // Binding logic... assume views exist in XML
            // TODO: Bind actual data if XML supports it. 
            // For now binding simple fields if ID match or just passing click
            
            // Assuming simple binding for now as XML content wasn't fully inspected but required for adapter update
            // tvAuthor.text = item.reviewerName
            // tvContent.text = item.content
            // ...
            
            btnMore.setOnClickListener { onMoreClick(item) }
        }
    }
}