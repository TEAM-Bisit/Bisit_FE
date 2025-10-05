package com.example.naottae.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.naottae.data.model.store.StoreItem
import com.example.naottae.databinding.ItemHomeStoreBinding

class HomeListAdapter(private val items: List<StoreItem>) :
    RecyclerView.Adapter<HomeListAdapter.StoreViewHolder>() {

    inner class StoreViewHolder(val binding: ItemHomeStoreBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreViewHolder {
        val binding = ItemHomeStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreViewHolder, position: Int) {
        val item = items[position]

        holder.binding.imgRecycler.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HomeListImageAdapter(item.images, item.hasVisitService)
        }

        holder.binding.tvName.text = item.name
        holder.binding.tvCategory.text = item.category
        holder.binding.tvRating.text = "★ ${item.rating}"
        holder.binding.tvReviewCount.text = "(${item.reviewCount})"
        holder.binding.tvOpenStatus.text = if (item.isOpen) "영업중" else "영업종료"
        holder.binding.tvBusinessHours.text = item.businessHours
        holder.binding.imgOpenStatus.setImageResource(
            if (item.isOpen) com.example.naottae.R.drawable.ic_open else com.example.naottae.R.drawable.ic_close
        )

        holder.binding.bottomDivider?.visibility = if (position == items.lastIndex) View.VISIBLE else View.GONE

        holder.binding.chipGroup.removeAllViews()
        val inflater = LayoutInflater.from(holder.itemView.context)
        item.tags.forEach { tag ->
            val chip = inflater.inflate(com.example.naottae.R.layout.item_home_list_tag, holder.binding.chipGroup, false) as TextView
            chip.text = tag
            holder.binding.chipGroup.addView(chip)
        }
    }

    override fun getItemCount() = items.size
}