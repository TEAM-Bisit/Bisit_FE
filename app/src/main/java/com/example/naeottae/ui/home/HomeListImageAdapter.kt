package com.example.naeottae.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.naeottae.databinding.ItemHomeStoreImageBinding

class HomeListImageAdapter(
    private val images: List<Int>,
    private val hasVisitService: Boolean
) : RecyclerView.Adapter<HomeListImageAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: ItemHomeStoreImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemHomeStoreImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.binding.imgStoreItem.setImageResource(images[position])
        holder.binding.chipVisitService.visibility =
            if (position == 0 && hasVisitService) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = images.size
}