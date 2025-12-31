package com.example.bisit.ui.customerMyReserve

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.reservation.ReservationInquiryData
import com.example.bisit.data.model.reservation.ReservationItem
import com.example.bisit.databinding.DialogCustomerMyReserveAskBinding
import com.example.bisit.databinding.DialogCancelReasonBinding
import android.app.Dialog
import androidx.core.widget.addTextChangedListener
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import retrofit2.Response

class CustomerMyReserveFragment : Fragment(R.layout.fragment_customer_my_reserve) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMyReserveAdapter

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

                fetchReservations(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun showCancelDialog(item: MyReserveItem) {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogCancelReasonBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvTitle.text = "예약 취소"
        dialogBinding.tvContentLabel.text = "${item.shopName} 예약을 취소하시겠습니까?"
        
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialogBinding.etReason.addTextChangedListener {
            val textLength = it?.length ?: 0
            dialogBinding.tvCharCount.text = "$textLength/50자"
            dialogBinding.btnSubmit.isEnabled = textLength > 0
            dialogBinding.btnSubmit.setTextColor(
                if (textLength > 0) resources.getColor(R.color.white, null) 
                else resources.getColor(R.color.muted_gray, null)
            )
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val reason = dialogBinding.etReason.text.toString()
            cancelReservation(item.reservationId, reason)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun cancelReservation(reservationId: String, reason: String) {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val request = com.example.bisit.data.model.reservation.CancelReservationRequest(reason)
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
        AlertDialog.Builder(requireContext())
            .setTitle("시술 확정")
            .setMessage("시술 완료를 확정하시겠습니까?")
            .setPositiveButton("확정하기") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val api = RetrofitClient.getReservationApi(requireContext())
                        val response = api.confirmReservation(reservationId.toLong())
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(requireContext(), "시술이 확정되었습니다.", Toast.LENGTH_SHORT).show()
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
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun fetchReservations(position: Int) {


        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getReservationApi(requireContext())
                val response = when (position) {
                    0 -> api.getScheduledReservations()
                    1 -> api.getCompletedReservations()
                    2 -> api.getCanceledReservations()
                    else -> api.getScheduledReservations()
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
                            reservedDate = res.reservedDate
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
        dialogBinding.tvDialogTitle.text = data.shopName
        dialogBinding.tvSellerPhone.text = if (data.staffName.isNullOrEmpty()) "정보 없음" else data.staffName
        dialogBinding.tvCustomerPhone.text = if (data.phoneNumber.isNullOrEmpty()) "정보 없음" else data.phoneNumber
        
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
        return when (tabPosition) {
            0 -> "예약"
            1 -> "완료"
            2 -> "취소"
            else -> status
        }
    }
}
