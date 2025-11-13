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

        adapter = CustomerMyReserveAdapter {
            findNavController().navigate(R.id.action_customerMyReserve_to_detail)
        }

        recyclerView.adapter = adapter
        adapter.setItems(R.layout.item_customer_my_reserve_wait)

        val tabs = listOf("예정", "완료", "취소")
        tabs.forEach { tabLayout.addTab(tabLayout.newTab().setText(it)) }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position ?: 0) {
                    0 -> adapter.setItems(R.layout.item_customer_my_reserve_wait)
                    1 -> adapter.setItems(R.layout.item_customer_my_reserve_completed)
                    2 -> adapter.setItems(R.layout.item_customer_my_reserve_canceled)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
}
