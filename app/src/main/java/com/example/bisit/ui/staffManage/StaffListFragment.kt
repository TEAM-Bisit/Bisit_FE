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
import com.example.bisit.ui.staffManage.adapter.StaffListAdapter

class StaffListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStaffList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = StaffListAdapter(getDummyStaffs())

        val sortToggle = view.findViewById<ImageView>(R.id.ivSortList)
        sortToggle.setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun getDummyStaffs(): List<String> =
        listOf("직원 A", "직원 B", "직원 C", "직원 D")
}
