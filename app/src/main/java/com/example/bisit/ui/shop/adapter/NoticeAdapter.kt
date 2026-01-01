package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemNoticeBinding
import com.example.bisit.data.model.shop.ShopNoticeResponse

class NoticeAdapter(
    private val onMoreClick: (ShopNoticeResponse) -> Unit
) : ListAdapter<ShopNoticeResponse, NoticeAdapter.VH>(DIFF) {

    companion object DIFF : DiffUtil.ItemCallback<ShopNoticeResponse>() {
        override fun areItemsTheSame(
            oldItem: ShopNoticeResponse,
            newItem: ShopNoticeResponse
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: ShopNoticeResponse,
            newItem: ShopNoticeResponse
        ): Boolean = oldItem == newItem
    }

    inner class VH(val b: ItemNoticeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(
            ItemNoticeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.b.apply {
            tvTitle.text = item.title
            tvContent.text = item.content
            tvDate.text = item.createdAt.substring(0, 10) // yyyy-MM-dd
            btnMore.setOnClickListener { onMoreClick(item) }
        }
    }
}
