package com.example.bisit.ui.staffManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.repository.staffManage.StaffManageRepository
import com.example.bisit.ui.staffManage.adapter.StaffRequestAdapter
import com.example.bisit.ui.staffManage.modal.StaffRequestResultDialog
import kotlinx.coroutines.launch

class StaffRequestsFragment : Fragment() {

    private lateinit var viewModel: StaffManageViewModel
    private lateinit var adapter: StaffRequestAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_staff_requests, container, false)

        // API / Repository / ViewModel
        val api = RetrofitClient.getStaffManageApi(requireContext())
        val repository = StaffManageRepository(api)

        viewModel = ViewModelProvider(
            this,
            StaffManageViewModelFactory(repository)
        )[StaffManageViewModel::class.java]

        // RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStaffRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StaffRequestAdapter(
            onApprove = { staffId ->
                viewModel.approveStaff(getShopId(), staffId)
                showResultDialog("직원 요청이 승인되었습니다.")
            },
            onReject = { staffId ->
                viewModel.rejectStaff(getShopId(), staffId)
                showResultDialog("직원 요청이 거절되었습니다.")
            }
        )
        recyclerView.adapter = adapter

        // 직원 신청 목록 최초 로드
        viewModel.loadPendingStaffs(getShopId())

        // 목록 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingStaffs.collect { list ->
                adapter.submitList(list)
            }
        }

        val sortToggle = view.findViewById<ImageView>(R.id.ivSortRequest)
        sortToggle.setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showResultDialog(message: String) {
        StaffRequestResultDialog(message)
            .show(parentFragmentManager, "staff_request_result")
    }

    private fun getShopId(): Long {
        return requireArguments().getLong("shopId")
    }
}
