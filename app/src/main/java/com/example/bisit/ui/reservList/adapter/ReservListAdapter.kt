package com.example.bisit.ui.reservList.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.ui.reservList.model.ReservListItem
import com.google.android.material.button.MaterialButton

class ReservListAdapter(
    private var items: List<ReservListItem>,
    private val onClick: (ReservListItem) -> Unit
) : RecyclerView.Adapter<ReservListAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvServiceName: TextView = view.findViewById(R.id.tvServiceName)
        val tvDate: TextView = view.findViewById(R.id.tvVisitTime)
        val tvStatus: TextView = view.findViewById(R.id.tvReservStatus)
        val tvPaymentStatus: TextView = view.findViewById(R.id.tvPaymentStatus)
        val btnDetail: MaterialButton = view.findViewById(R.id.btnDetail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reserv_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        // 시술명 / 날짜
        holder.tvServiceName.text = item.serviceName
        holder.tvDate.text = item.dateTime

        // 예약 상태
        holder.tvStatus.text = item.status
        if (item.status == "예약 확정") {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.red_FE6B6B))

            holder.tvPaymentStatus.text = "입금 완료"
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_payment_confirmed)
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.bottom_nav_unselected))

            holder.tvPaymentStatus.text = "입금 완료"
            holder.tvPaymentStatus.setTextColor(ContextCompat.getColor(context, R.color.gray_515965))
            holder.tvPaymentStatus.setBackgroundResource(R.drawable.bg_payment_confirming)
        }

        // 상세보기 버튼
        holder.btnDetail.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newList: List<ReservListItem>) {
        val diffCallback = ReservListDiffCallback(items, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        items = newList
        diffResult.dispatchUpdatesTo(this)
    }

    class ReservListDiffCallback(
        private val oldList: List<ReservListItem>,
        private val newList: List<ReservListItem>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
