package com.example.bisit.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemServiceBinding
import com.example.bisit.data.model.shop.TreatmentResponse

class ServiceAdapter(
    private val onMoreClick: (TreatmentResponse) -> Unit
) : ListAdapter<TreatmentResponse, ServiceAdapter.VH>(DIFF) {

    companion object DIFF : DiffUtil.ItemCallback<TreatmentResponse>() {

        override fun areItemsTheSame(
            oldItem: TreatmentResponse,
            newItem: TreatmentResponse
        ): Boolean {
            return oldItem.treatmentId == newItem.treatmentId
        }

        override fun areContentsTheSame(
            oldItem: TreatmentResponse,
            newItem: TreatmentResponse
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class VH(val b: ItemServiceBinding) :
        RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemServiceBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.b.apply {
            tvTitle.text = item.name
            tvDesc.text = item.description
            tvPrice.text = "%,d원".format(item.price)

            btnMore.setOnClickListener {
                onMoreClick(item)
            }
        }
    }
}
