package com.example.bisit.ui.todayReserv.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.databinding.ItemApprovedReservationBinding

data class ApprovedReservation(
    val reservationId: String,
    val shopName: String,
    val date: String,
    val address: String,
    var status: String
)

class ApprovedReservationAdapter(
    private val onChangeStatus: (ApprovedReservation) -> Unit
) : ListAdapter<ApprovedReservation, ApprovedReservationAdapter.ViewHolder>(ApprovedDiffCallback()) {

    inner class ViewHolder(private val binding: ItemApprovedReservationBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ApprovedReservation) = with(binding) {
            tvStatus.text = item.status
            tvReservationId.text = item.reservationId
            tvShopName.text = item.shopName
            tvDate.text = item.date
            tvAddress.text = item.address

            val colorRes = if (item.status == "예약 확정") {
                R.color.red_FE6B6B
            } else {
                R.color.gray_515965
            }
            tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

            btnChangeStatus.setOnClickListener { onChangeStatus(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApprovedReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ApprovedDiffCallback : DiffUtil.ItemCallback<ApprovedReservation>() {
    override fun areItemsTheSame(oldItem: ApprovedReservation, newItem: ApprovedReservation): Boolean {
        return oldItem.reservationId == newItem.reservationId
    }

    override fun areContentsTheSame(oldItem: ApprovedReservation, newItem: ApprovedReservation): Boolean {
        return oldItem == newItem
    }
}
