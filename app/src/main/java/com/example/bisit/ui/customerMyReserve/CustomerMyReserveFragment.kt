package com.example.bisit.ui.customerMyReserve

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.google.android.material.tabs.TabLayout

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
        
        // Initial data for tab 0 (Wait)
        updateList(0)

        val tabs = listOf("예정", "완료", "취소")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateList(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateList(position: Int) {
        val items = when (position) {
            0 -> listOf(
                MyReserveItem("id_wait_1", "Shop A", "예약"),
                MyReserveItem("id_wait_2", "Shop B", "예약")
            )
            1 -> listOf(
                MyReserveItem("id_done_1", "Shop C", "완료"), // This ID will be used for Review
                MyReserveItem("id_done_2", "Shop D", "완료")
            )
            2 -> listOf(
                MyReserveItem("id_cancel_1", "Shop E", "취소")
            )
            else -> emptyList()
        }
        adapter.setItems(items)
    }
}
