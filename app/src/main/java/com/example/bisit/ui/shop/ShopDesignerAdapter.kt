package com.example.bisit.ui.shop

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.shop.Designer

class ShopDesignerAdapter(
    private val items: List<Designer>,
    private val itemClick: (Int) -> Unit
) : RecyclerView.Adapter<ShopDesignerAdapter.VH>() {

    var selectedPosition: Int = -1

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: LinearLayout = itemView.findViewById(R.id.container)
        val tvName: TextView = itemView.findViewById(R.id.tv_name)
        val tvRole: TextView = itemView.findViewById(R.id.tv_role)
        val tvIntro: TextView = itemView.findViewById(R.id.tv_intro)
        val tvRating: TextView = itemView.findViewById(R.id.tv_rating)
        val tvReviewCount: TextView = itemView.findViewById(R.id.tv_review_count)
        val imgAvatar: ImageView = itemView.findViewById(R.id.img_avatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_designer, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.tvRole.text = item.role
        holder.tvIntro.text = item.intro
        holder.tvRating.text = item.rating
        holder.tvReviewCount.text = item.reviewCount
        holder.imgAvatar.setImageResource(R.drawable.img_designer)

        val isSelected = position == selectedPosition
        holder.itemView.isSelected = isSelected
        holder.container.isSelected = isSelected

        if (isSelected) {
            holder.tvName.setTextColor(Color.parseColor("#4076FF"))
        } else {
            holder.tvName.setTextColor(Color.parseColor("#222222"))
        }

        holder.itemView.setOnClickListener {
            itemClick(position)
        }
    }

    override fun getItemCount(): Int = items.size
}