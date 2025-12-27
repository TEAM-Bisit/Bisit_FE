package com.example.bisit.ui.staffManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.repository.staffManage.StaffManageRepository
import com.example.bisit.ui.staffManage.adapter.StaffListAdapter
import com.example.bisit.ui.staffManage.modal.DeleteCompleteDialog
import com.example.bisit.ui.staffManage.modal.DeleteStaffDialog
import kotlinx.coroutines.launch

class StaffListFragment : Fragment() {

    // StaffManage 전체에서 공유하는 ViewModel
    private val viewModel: StaffManageViewModel by activityViewModels {
        StaffManageViewModelFactory(
            StaffManageRepository(
                RetrofitClient.getStaffManageApi(requireContext())
            )
        )
    }

    private lateinit var adapter: StaffListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_staff_list, container, false)

        adapter = StaffListAdapter(
            onDeleteClick = { staff ->
                showDeleteDialog(staff.staffId)
            }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStaffList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // 승인된 직원 목록
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvedStaffs.collect { list ->
                adapter.submitList(list)
            }
        }

        // 최초 로드
        viewModel.loadApprovedStaffs(getShopId())

        val sortToggle = view.findViewById<ImageView>(R.id.ivSortList)
        sortToggle.setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showDeleteDialog(staffId: Long) {
        DeleteStaffDialog(
            onConfirm = {
                // 직원 삭제 (리스트 즉시 반영)
                viewModel.deleteStaff(getShopId(), staffId)

                // ️기존 삭제 확인 모달 닫기
                (parentFragmentManager.findFragmentByTag("delete_staff")
                        as? DeleteStaffDialog)?.dismiss()

                // 삭제 완료 모달 표시
                DeleteCompleteDialog().show(
                    parentFragmentManager,
                    "delete_complete"
                )
            }
        ).show(parentFragmentManager, "delete_staff")
    }

    private fun getShopId(): Long {
        return requireArguments().getLong("shopId")
    }
}
