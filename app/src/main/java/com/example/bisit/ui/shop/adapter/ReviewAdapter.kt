package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemReviewBinding
import com.example.bisit.ui.shop.model.Review

class ReviewAdapter(private val onMoreClick: (Review) -> Unit) :
    ListAdapter<Review, ReviewAdapter.VH>(DIFF) {


    companion object DIFF : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(oldItem: Review, newItem: Review) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Review, newItem: Review) = oldItem == newItem
    }


    inner class VH(val b: ItemReviewBinding) : RecyclerView.ViewHolder(b.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.apply {
// 실데이터 바인딩은 XML에 dataBinding 없이 단순 텍스트로 가정
            btnMore.setOnClickListener { onMoreClick(item) }
        }
    }
}