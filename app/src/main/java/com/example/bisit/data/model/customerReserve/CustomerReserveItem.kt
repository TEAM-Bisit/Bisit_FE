package com.example.bisit.data.model.customerReserve

sealed class CustomerReserveItem {
    data class DesignerInfo(val name: String, val recentCount: Int, val profileRes: Int) : CustomerReserveItem()
    data class DesignerComment(val comment: String) : CustomerReserveItem()
    data class ServiceMenu(val title: String, val timeSlots: List<String>) : CustomerReserveItem()
}