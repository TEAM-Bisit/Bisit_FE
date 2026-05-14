package kr.bisit.app.ui.staffManage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.repository.staffManage.StaffManageRepository
import kr.bisit.app.ui.shop.ShopRegisterViewModel
import kr.bisit.app.ui.shop.ShopRegisterViewModelFactory
import kr.bisit.app.ui.staffManage.adapter.StaffRequestAdapter
import kr.bisit.app.ui.staffManage.modal.StaffRequestResultDialog
import kotlinx.coroutines.launch

class StaffRequestsFragment : Fragment() {

    private lateinit var viewModel: StaffManageViewModel
    private lateinit var adapter: StaffRequestAdapter

    // ⭐️ shopId 단일 소스
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_staff_requests, container, false)

        val api = RetrofitClient.getStaffManageApi(requireContext())
        val repository = StaffManageRepository(api)

        viewModel = ViewModelProvider(
            this,
            StaffManageViewModelFactory(repository)
        )[StaffManageViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvStaffRequests)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = StaffRequestAdapter(
            onApprove = { staffId ->
                shopRegisterViewModel.shopId.value?.let { shopId ->
                    viewModel.approveStaff(shopId, staffId)
                    showResultDialog("직원 요청이 승인되었습니다.")
                }
            },
            onReject = { staffId ->
                shopRegisterViewModel.shopId.value?.let { shopId ->
                    viewModel.rejectStaff(shopId, staffId)
                    showResultDialog("직원 요청이 거절되었습니다.")
                }
            }
        )
        recyclerView.adapter = adapter

        // shopId 관찰해서 최초 로드
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId ?: return@collect
                viewModel.loadPendingStaffs(shopId)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.pendingStaffs.collect { list ->
                adapter.submitList(list)
            }
        }

        view.findViewById<ImageView>(R.id.ivSortRequest).setOnClickListener {
            Toast.makeText(requireContext(), "정렬 기준 변경", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun showResultDialog(message: String) {
        StaffRequestResultDialog(message)
            .show(parentFragmentManager, "staff_request_result")
    }
}
