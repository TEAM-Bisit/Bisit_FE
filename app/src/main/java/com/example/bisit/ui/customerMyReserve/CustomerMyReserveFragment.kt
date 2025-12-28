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
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class CustomerMyReserveFragment : Fragment(R.layout.fragment_customer_my_reserve) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CustomerMyReserveAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.recyclerReserve)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = CustomerMyReserveAdapter { item ->
            val bundle = Bundle().apply {
                putString("reservationId", item.reservationId)
            }
            findNavController().navigate(R.id.action_customerMyReserve_to_detail, bundle)
        }

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
                    val reservations = response.body()?.data?.reservations?.content ?: emptyList()
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
    
    private fun mapStatusToDisplayText(status: String, tabPosition: Int): String {
        return when (tabPosition) {
            0 -> "예약"
            1 -> "완료"
            2 -> "취소"
            else -> status
        }
    }
}
