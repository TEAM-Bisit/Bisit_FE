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
import com.example.bisit.ui.shop.ShopRegisterViewModel
import com.example.bisit.ui.shop.ShopRegisterViewModelFactory
import com.example.bisit.ui.staffManage.adapter.StaffListAdapter
import com.example.bisit.ui.staffManage.modal.DeleteCompleteDialog
import com.example.bisit.ui.staffManage.modal.DeleteStaffDialog
import kotlinx.coroutines.launch

class StaffListFragment : Fragment() {

    // 직원 관리 ViewModel (기존 그대로)
    private val viewModel: StaffManageViewModel by activityViewModels {
        StaffManageViewModelFactory(
            StaffManageRepository(
                RetrofitClient.getStaffManageApi(requireContext())
            )
        )
    }

    // shopId 단일 소스
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
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

        // 승인된 직원 목록 관찰
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.approvedStaffs.collect { list ->
                adapter.submitList(list)
            }
        }

        // shopId 도착 시점에만 API 호출
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId ?: return@collect
                viewModel.loadApprovedStaffs(shopId)
            }
        }

        val sortToggle = view.findViewById<ImageView>(R.id.ivSortList)
        sortToggle.setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showDeleteDialog(staffId: Long) {
        DeleteStaffDialog(
            onConfirm = {
                val shopId = shopRegisterViewModel.shopId.value ?: return@DeleteStaffDialog

                viewModel.deleteStaff(shopId, staffId)

                (parentFragmentManager.findFragmentByTag("delete_staff")
                        as? DeleteStaffDialog)?.dismiss()

                DeleteCompleteDialog().show(
                    parentFragmentManager,
                    "delete_complete"
                )
            }
        ).show(parentFragmentManager, "delete_staff")
    }
}
