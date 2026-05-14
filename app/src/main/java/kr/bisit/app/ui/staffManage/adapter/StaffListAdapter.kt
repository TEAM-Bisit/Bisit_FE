package kr.bisit.app.ui.staffManage.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R
import kr.bisit.app.data.model.staffManage.ApprovedStaffItem

class StaffListAdapter(
    private val onDeleteClick: (ApprovedStaffItem) -> Unit
) : ListAdapter<ApprovedStaffItem, StaffListAdapter.StaffViewHolder>(
    StaffListDiffCallback
) {

    inner class StaffViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvStaffName)
        val btnDelete: TextView = itemView.findViewById(R.id.btnStaffDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StaffViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_staff_list, parent, false)
        return StaffViewHolder(view)
    }

    override fun onBindViewHolder(holder: StaffViewHolder, position: Int) {
        val item = getItem(position)

        holder.tvName.text = item.name
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }
}
