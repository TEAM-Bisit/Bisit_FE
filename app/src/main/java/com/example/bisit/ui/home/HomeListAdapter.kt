package com.example.bisit.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.data.model.category.CategoryShopItem
import com.example.bisit.databinding.ItemHomeStoreBinding

class HomeListAdapter(
    private var items: List<CategoryShopItem>,
    private val onClick: (CategoryShopItem) -> Unit
) : RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHomeStoreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeStoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        binding.imgRecycler.layoutManager =
            LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvName.text = item.shopName
        holder.binding.tvCategory.text = item.category
        holder.binding.tvRating.text = item.averageRating.toString()
        holder.binding.tvReviewCount.text = "(${item.reviewCount})"
        holder.binding.tvBusinessHours.text = item.businessHours ?: ""

        android.util.Log.d("HomeListAdapter", "Binding shop: ${item.shopName}, photos: ${item.photos}")

        val imageAdapter =
            HomeListImageAdapter(item.photos, item.hasVisitService ?: false)

        holder.binding.imgRecycler.adapter = imageAdapter

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<CategoryShopItem>) {
        val oldSize = items.size
        items = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun addData(newList: List<CategoryShopItem>) {
        val oldSize = items.size
        (items as MutableList).addAll(newList)
        notifyItemRangeInserted(oldSize, newList.size)
    }

    fun clearData() {
        val oldSize = items.size
        (items as MutableList).clear()
        notifyItemRangeRemoved(0, oldSize)
    }
}
