package com.example.bisit.ui.staffManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.ui.staffManage.adapter.StaffRequestAdapter

class StaffRequestsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff_requests, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStaffRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = StaffRequestAdapter(getDummyRequests())

        val sortToggle = view.findViewById<ImageView>(R.id.ivSortRequest)
        sortToggle.setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun getDummyRequests(): List<String> =
        listOf("요청 1", "요청 2", "요청 3", "요청 4")
}
