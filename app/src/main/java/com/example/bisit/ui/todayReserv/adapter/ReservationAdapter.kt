package com.example.bisit.ui.todayReserv.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemReservationBinding

data class Reservation(
    val reservationId: String,
    val shopName: String,
    val date: String,
    val address: String
)

class ReservationAdapter(
    private val onReject: (Reservation) -> Unit,
    private val onApprove: (Reservation) -> Unit
) : ListAdapter<Reservation, ReservationAdapter.ViewHolder>(ReservationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Reservation) {
            binding.tvShopName.text = item.shopName
            binding.tvDate.text = item.date
            binding.tvAddress.text = item.address

            binding.btnReject.setOnClickListener {
                onReject(item)
            }
            binding.btnApprove.setOnClickListener {
                onApprove(item)
            }
        }
    }

    class ReservationDiffCallback : DiffUtil.ItemCallback<Reservation>() {
        override fun areItemsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem.reservationId == newItem.reservationId
        }

        override fun areContentsTheSame(oldItem: Reservation, newItem: Reservation): Boolean {
            return oldItem == newItem
        }
    }
}
