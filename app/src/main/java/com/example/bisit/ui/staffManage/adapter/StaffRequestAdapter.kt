package com.example.bisit.ui.staffManage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.staffManage.PendingStaffItem
import com.example.bisit.ui.staffManage.adapter.StaffRequestDiffCallback


class StaffRequestAdapter(
    private val onApprove: (Long) -> Unit,
    private val onReject: (Long) -> Unit
) : ListAdapter<PendingStaffItem, StaffRequestAdapter.RequestViewHolder>(
    StaffRequestDiffCallback
) {

    inner class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStaffName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvStaffPhone)
        val btnApprove: MaterialButton = itemView.findViewById(R.id.btnApprove)
        val btnReject: MaterialButton = itemView.findViewById(R.id.btnReject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvName.text = item.name
        holder.tvPhone.text = item.phone

        holder.btnApprove.setOnClickListener {
            onApprove(item.staffId)
        }

        holder.btnReject.setOnClickListener {
            onReject(item.staffId)
        }
    }
}
