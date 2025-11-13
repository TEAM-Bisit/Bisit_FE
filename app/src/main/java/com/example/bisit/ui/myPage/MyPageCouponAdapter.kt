package com.example.bisit.ui.myPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemMypageCouponBinding

class MyPageCouponAdapter(private val coupons: List<Map<String, String>>) :
    RecyclerView.Adapter<MyPageCouponAdapter.CouponViewHolder>() {

    inner class CouponViewHolder(private val binding: ItemMypageCouponBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Map<String, String>) {
            binding.tvCouponTitlePercent.text = item["percent"]
            binding.tvCouponTitle.text = item["title"]
            binding.tvCouponDesc.text = item["desc"]
            binding.tvCouponMetaDday.text = item["dday"]
            binding.tvCouponMeta.text = item["validDate"]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding = ItemMypageCouponBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(coupons[position])
    }

    override fun getItemCount(): Int = coupons.size
}
