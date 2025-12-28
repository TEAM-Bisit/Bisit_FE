package com.example.bisit.ui.staffManage.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.bisit.data.model.staffManage.ApprovedStaffItem

object StaffListDiffCallback : DiffUtil.ItemCallback<ApprovedStaffItem>() {

    override fun areItemsTheSame(
        oldItem: ApprovedStaffItem,
        newItem: ApprovedStaffItem
    ): Boolean {
        // 같은 직원인지 판단 (고유 ID)
        return oldItem.staffId == newItem.staffId
    }

    override fun areContentsTheSame(
        oldItem: ApprovedStaffItem,
        newItem: ApprovedStaffItem
    ): Boolean {
        // 내용 변경 여부
        return oldItem == newItem
    }
}