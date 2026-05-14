package kr.bisit.app.ui.staffManage.adapter

import androidx.recyclerview.widget.DiffUtil
import kr.bisit.app.data.model.staffManage.PendingStaffItem

object StaffRequestDiffCallback : DiffUtil.ItemCallback<PendingStaffItem>() {

    override fun areItemsTheSame(
        oldItem: PendingStaffItem,
        newItem: PendingStaffItem
    ): Boolean {
        // 같은 직원인지 판단 (고유 ID)
        return oldItem.staffId == newItem.staffId
    }

    override fun areContentsTheSame(
        oldItem: PendingStaffItem,
        newItem: PendingStaffItem
    ): Boolean {
        return oldItem == newItem
    }
}