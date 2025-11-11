package com.example.bisit.ui.customerReserve

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.data.model.customerReserve.CustomerReserveItem
import com.example.bisit.databinding.ItemDesignerCommentBinding
import com.example.bisit.databinding.ItemDesignerInfoBinding
import com.example.bisit.databinding.ItemServiceMenuBinding
import com.google.android.material.card.MaterialCardView

class CustomerReserveAdapter(
    private var items: List<CustomerReserveItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPosition: Int = -1

    companion object {
        private const val TYPE_DESIGNER_INFO = 0
        private const val TYPE_COMMENT = 1
        private const val TYPE_SERVICE_MENU = 2
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is CustomerReserveItem.DesignerInfo -> TYPE_DESIGNER_INFO
        is CustomerReserveItem.DesignerComment -> TYPE_COMMENT
        is CustomerReserveItem.ServiceMenu -> TYPE_SERVICE_MENU
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DESIGNER_INFO -> DesignerInfoViewHolder(
                ItemDesignerInfoBinding.inflate(inflater, parent, false)
            )
            TYPE_COMMENT -> DesignerCommentViewHolder(
                ItemDesignerCommentBinding.inflate(inflater, parent, false)
            )
            TYPE_SERVICE_MENU -> ServiceMenuViewHolder(
                ItemServiceMenuBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CustomerReserveItem.DesignerInfo -> (holder as DesignerInfoViewHolder).bind(item)
            is CustomerReserveItem.DesignerComment -> (holder as DesignerCommentViewHolder).bind(item)
            is CustomerReserveItem.ServiceMenu -> (holder as ServiceMenuViewHolder).bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<CustomerReserveItem>) {
        items = newItems
        selectedPosition = -1
        notifyDataSetChanged()
    }

    class DesignerInfoViewHolder(private val binding: ItemDesignerInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomerReserveItem.DesignerInfo) {
            binding.tvDesignerName.text = item.name
            binding.tvRecentCount.text = "최근 시술 ${item.recentCount}회"
            binding.ivProfile.setImageResource(item.profileRes)
        }
    }

    class DesignerCommentViewHolder(private val binding: ItemDesignerCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomerReserveItem.DesignerComment) {
            binding.tvCommentContent.text = item.comment
        }
    }

    inner class ServiceMenuViewHolder(private val binding: ItemServiceMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomerReserveItem.ServiceMenu, position: Int) {
            binding.tvServiceName.text = item.title
            binding.tvServiceTime.text =
                if (item.timeSlots.isNotEmpty()) item.timeSlots.first() else ""
            binding.tvServiceDescription.visibility = android.view.View.GONE
            binding.tvServicePrice.visibility = android.view.View.GONE

            val card = binding.root as MaterialCardView
            card.strokeWidth = 1
            card.strokeColor = if (selectedPosition == position)
                Color.parseColor("#FE6B6B")
            else
                Color.TRANSPARENT

            card.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = if (selectedPosition == position) -1 else position
                notifyItemChanged(previousPosition)
                notifyItemChanged(position)
            }
        }
    }
}

