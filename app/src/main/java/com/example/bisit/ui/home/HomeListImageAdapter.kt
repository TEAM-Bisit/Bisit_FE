package com.example.bisit.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bisit.databinding.ItemHomeStoreImageBinding

class HomeListImageAdapter(
    private val images: List<String>,
    private val hasVisitService: Boolean
) : RecyclerView.Adapter<HomeListImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemHomeStoreImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemHomeStoreImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        Glide.with(holder.itemView.context)
            .load(images[position])
            .into(holder.binding.imgStoreItem)

        holder.binding.chipVisitService.visibility =
            if (position == 0 && hasVisitService) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = images.size
}
