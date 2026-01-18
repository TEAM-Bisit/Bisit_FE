package com.example.bisit.data.repository.staffManage

import com.example.bisit.data.api.StaffManageApiService
import com.example.bisit.data.model.staffManage.*

class StaffManageRepository(
    private val api: StaffManageApiService
) {

    suspend fun addStaff(
        shopId: Long,
        name: String,
        email: String,
        phone: String
    ): StaffResponse {
        return api.addStaff(
            shopId,
            AddStaffRequest(name, email, phone)
        ).data
    }

    suspend fun deleteStaff(
        shopId: Long,
        staffId: Long
    ): String {
        return api.deleteStaff(shopId, staffId).data
    }

    suspend fun approveStaff(
        shopId: Long,
        staffId: Long
    ): StaffResponse {
        return api.updateStaffStatus(
            shopId,
            staffId,
            StaffStatus.APPROVED
        ).data
    }

    suspend fun rejectStaff(
        shopId: Long,
        staffId: Long
    ): StaffResponse {
        return api.updateStaffStatus(
            shopId,
            staffId,
            StaffStatus.REJECTED
        ).data
    }

    suspend fun getPendingStaffs(shopId: Long): List<PendingStaffItem> {
        return api.getPendingStaffs(shopId).data
    }

    suspend fun getApprovedStaffs(shopId: Long): List<ApprovedStaffItem> {
        return api.getApprovedStaffs(shopId).data
    }

    suspend fun hasPendingStaff(shopId: Long): Boolean {
        return api.getPendingStaffs(shopId).data.isNotEmpty()
    }
}
