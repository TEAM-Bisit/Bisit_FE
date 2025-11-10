package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemServiceBinding
import com.example.bisit.ui.shop.model.ServiceItem

class ServiceAdapter(private val onMoreClick: (ServiceItem) -> Unit) :
    ListAdapter<ServiceItem, ServiceAdapter.VH>(DIFF) {


    companion object DIFF : DiffUtil.ItemCallback<ServiceItem>() {
        override fun areItemsTheSame(oldItem: ServiceItem, newItem: ServiceItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ServiceItem, newItem: ServiceItem) = oldItem == newItem
    }


    inner class VH(val b: ItemServiceBinding) : RecyclerView.ViewHolder(b.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false))


    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.apply {
            tvTitle.text = item.title
            tvDesc.text = item.desc
            tvPrice.text = "%,d원".format(item.price)
            btnMore.setOnClickListener { onMoreClick(item) }
        }
    }
}