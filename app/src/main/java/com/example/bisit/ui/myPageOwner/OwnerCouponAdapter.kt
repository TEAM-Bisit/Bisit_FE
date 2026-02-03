package com.example.bisit.ui.myPageOwner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemOwnerCouponBinding

data class OwnerCoupon(
    val id: String,
    val value: String,
    val name: String,
    val description: String,
    val remainingDays: Int,
    val expiryDate: String
)

class OwnerCouponAdapter(
    private val onMoreClicked: (OwnerCoupon) -> Unit
) : ListAdapter<OwnerCoupon, OwnerCouponAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemOwnerCouponBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OwnerCoupon, onMoreClicked: (OwnerCoupon) -> Unit) {
            binding.tvCouponValue.text = item.value
            binding.tvCouponName.text = item.name
            binding.tvCouponDescription.text = item.description
            binding.tvRemainingDays.text = "${item.remainingDays}일 남음"
            binding.tvExpiryDate.text = "${item.expiryDate}까지 사용 가능"
            binding.btnMore.setOnClickListener { onMoreClicked(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnerCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onMoreClicked)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OwnerCoupon>() {
        override fun areItemsTheSame(oldItem: OwnerCoupon, newItem: OwnerCoupon) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: OwnerCoupon, newItem: OwnerCoupon) = oldItem == newItem
    }
}
