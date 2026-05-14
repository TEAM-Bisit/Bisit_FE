package kr.bisit.app.ui.customerMyReserve

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.reservation.ReservationInquiryData
import kr.bisit.app.data.model.reservation.ReservationItem
import kr.bisit.app.databinding.DialogCustomerMyReserveAskBinding
import kr.bisit.app.databinding.DialogCancelReasonBinding
import android.app.Dialog
import androidx.core.widget.addTextChangedListener
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import kr.bisit.app.ui.dialog.CustomTwoButtonDialog

class CustomerMyReserveFragment : Fragment(R.layout.fragment_customer_my_reserve) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMyReserveAdapter
    
    // 신규: 정렬 상태 관리 (초기값: 내림차순 - 최신순)
    private var currentSortDirection = "desc"
    private var currentTabPosition = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.recyclerReserve)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CustomerMyReserveAdapter(
            onDetailClick = { item ->
                val bundle = Bundle().apply {
                    putString("reservationId", item.reservationId)
                    putString("status", item.status)
                }
                findNavController().navigate(R.id.action_customerMyReserve_to_detail, bundle)
            },
            onInquireClick = { item ->
                fetchInquiryInfo(item.reservationId)
            },
            onCancelClick = { item ->
                showCancelDialog(item)
            },
            onConfirmClick = { item ->
                confirmReservation(item.reservationId)
            }
        )

        recyclerView.adapter = adapter
        
        // Initial data for tab 0 (Scheduled)
        fetchReservations(0)

        val tabs = listOf("예정", "완료", "취소")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTabPosition = tab?.position ?: 0
                fetchReservations(currentTabPosition)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 정렬 버튼 클릭 리스너 설정
        val tvSortStandard = view.findViewById<android.widget.TextView>(R.id.tvSortStandard)
        val ivSortIcon = view.findViewById<android.widget.ImageView>(R.id.ivSortIcon)

        val sortClickListener = View.OnClickListener {
            toggleSortDirection(tvSortStandard)
        }
        
        tvSortStandard?.setOnClickListener(sortClickListener)
        ivSortIcon?.setOnClickListener(sortClickListener)
        
        // 초기 정렬 텍스트 설정
        tvSortStandard?.let { updateSortText(it) }
    }

    override fun onResume() {
        super.onResume()
        // Refresh the current list when coming back from detail screen
        fetchReservations(currentTabPosition)
    }

    private fun toggleSortDirection(tvSortStandard: android.widget.TextView) {
        currentSortDirection = if (currentSortDirection == "desc") "asc" else "desc"
        updateSortText(tvSortStandard)
        fetchReservations(currentTabPosition)
    }

    private fun updateSortText(tvSortStandard: android.widget.TextView) {
        tvSortStandard.text = if (currentSortDirection == "desc") "최신순" else "오래된순"
    }

    private fun showCancelDialog(item: MyReserveItem) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogCancelReasonBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        // 배경을 투명하게 설정 (둥근 모서리나 커스텀 배경 적용을 위해 필요)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "예약 취소"
        dialogBinding.tvContentLabel.text = "${item.shopName} 예약을 취소하시겠습니까?"

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialogBinding.etReason.addTextChangedListener {
            val textLength = it?.length ?: 0
            dialogBinding.tvCharCount.text = "$textLength/50자"
            dialogBinding.btnSubmit.isEnabled = textLength > 0

            // 버튼 색상 변경 (Context 안정성을 위해 requireContext() 사용)
            dialogBinding.btnSubmit.setTextColor(
                if (textLength > 0) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.muted_gray)
            )
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val reason = dialogBinding.etReason.text.toString()
            cancelReservation(item.reservationId, reason)
            dialog.dismiss()
        }

        // 1. 먼저 다이어로그를 보여줍니다.
        dialog.show()

        // 2. 보여준 직후에 윈도우 크기를 화면에 맞게 재설정합니다.
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            // 화면 너비의 90% 정도로 설정 (원하는 비율로 조정 가능)
            val displayMetrics = resources.displayMetrics
            params.width = (displayMetrics.widthPixels * 0.9).toInt()
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
    }

    private fun cancelReservation(reservationId: String, reason: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val request = kr.bisit.app.data.model.reservation.CancelReservationRequest(reason)
                val response = api.cancelReservation(reservationId.toLong(), request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(requireContext(), "예약이 취소되었습니다.", Toast.LENGTH_SHORT).show()
                    // Refresh current tab (Scheduled)
                    fetchReservations(0)
                } else {
                    Log.e("CustomerMyReserve", "Failed to cancel reservation: ${response.code()}")
                    Toast.makeText(requireContext(), "예약 취소에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CustomerMyReserve", "Error canceling reservation", e)
                Toast.makeText(requireContext(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmReservation(reservationId: String) {
        CustomTwoButtonDialog(
            title = "시술 확정",
            subtitle = "시술 완료를 확정하시겠습니까?",
            positiveButtonText = "확정하기",
            negativeButtonText = "취소",
            onPositiveClick = {
                lifecycleScope.launch {
                    try {
                        val api = RetrofitClient.getReservationApi(requireContext())
                        val response = api.confirmReservation(reservationId.toLong())
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "시술이 확정되었습니다 (OK).", Toast.LENGTH_SHORT).show()
                            // Refresh current tab (Completed)
                            fetchReservations(1)
                        } else {
                            Log.e("CustomerMyReserve", "Failed to confirm reservation: ${response.code()}")
                            Toast.makeText(requireContext(), "시술 확정에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("CustomerMyReserve", "Error confirming reservation", e)
                        Toast.makeText(requireContext(), "에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        ).show(childFragmentManager, "ConfirmDialog")
    }

    private fun fetchReservations(position: Int) {
        // Debug Toast to check if this is called unexpectedly
        // Toast.makeText(requireContext(), "Fetching list: $position", Toast.LENGTH_SHORT).show()


        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = when (position) {
                    0 -> api.getScheduledReservations(sortDirection = currentSortDirection)
                    1 -> api.getCompletedReservations(sortDirection = currentSortDirection)
                    2 -> api.getCanceledReservations(sortDirection = currentSortDirection)
                    else -> api.getScheduledReservations(sortDirection = currentSortDirection)
                }
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val reservations: List<ReservationItem> = response.body()?.data?.reservations?.content ?: emptyList()
                    val items = reservations.map { res ->
                        MyReserveItem(
                            reservationId = res.reservationId.toString(),
                            orderId = res.orderId,
                            shopName = res.shopName,
                            status = mapStatusToDisplayText(res.status, position),
                            treatmentName = res.treatmentName,
                            price = res.price,
                            reservedDate = res.reservedDate,
                            isConfirmed = res.status.uppercase() == "CUSTOMER_CONFIRMED"
                        )
                    }
                    adapter.setItems(items)
                } else {
                    Log.e("CustomerMyReserve", "Failed to fetch reservations: ${response.code()}")
                    adapter.setItems(emptyList())
                }
            } catch (e: Exception) {
                Log.e("CustomerMyReserve", "Error fetching reservations", e)
                Toast.makeText(requireContext(), "예약 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                adapter.setItems(emptyList())
            }
        }
    }

    
    
    private fun fetchInquiryInfo(reservationId: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = api.getReservationInquiry(reservationId.toLong())
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let {
                        showInquireDialog(it)
                    }
                }
            } catch (e: Exception) {
                Log.e("CustomerMyReserve", "Error fetching inquiry info", e)
                Toast.makeText(requireContext(), "문의 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInquireDialog(data: ReservationInquiryData) {
        val dialogBinding = DialogCustomerMyReserveAskBinding.inflate(LayoutInflater.from(requireContext()))
        
        Log.d("CustomerMyReserve", "Showing inquiry dialog. Shop: ${data.shopName}, Staff: ${data.staffName}, Phone: ${data.phoneNumber}")
        dialogBinding.tvDialogTitle.text = "문의하기"
        dialogBinding.tvStaffName.text = if (data.shopName.isNullOrEmpty()) "정보 없음" else data.shopName
        dialogBinding.tvShopPhone.text = if (data.phoneNumber.isNullOrEmpty()) "정보 없음" else data.phoneNumber
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun mapStatusToDisplayText(status: String, tabPosition: Int): String {
        if (status.uppercase() == "CUSTOMER_CONFIRMED") return "확정됨"
        return when (tabPosition) {
            0 -> "예약"
            1 -> "완료"
            2 -> "취소"
            else -> status
        }
    }
}
