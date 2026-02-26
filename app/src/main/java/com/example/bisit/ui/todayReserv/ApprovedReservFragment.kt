package com.example.bisit.ui.todayReserv

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.MainActivity
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.todayReservation.ChangeStatusRequest
import com.example.bisit.data.model.todayReservation.ReservationItem
import com.example.bisit.data.repository.todayReservation.TodayReservationRepository
import com.example.bisit.databinding.FragmentApprovedReservBinding
import com.example.bisit.ui.todayReserv.adapter.TodayReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ChangeStatusDialog
import kotlinx.coroutines.launch

class ApprovedReservFragment : Fragment(), SortableFragment, TodayStatusTargetProvider {

    companion object { private const val TAG = "ApprovedReserv" }

    private var _binding: FragmentApprovedReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var reservationList = mutableListOf<ReservationItem>()
    private var sortBy: String = "recent"
    private lateinit var repository: TodayReservationRepository

    // ✅ 온보딩에서 쓸 "상태변경 버튼" View 캐시
    private var changeStatusBtnForGuide: View? = null
    override fun getChangeStatusButtonForGuide(): View? = changeStatusBtnForGuide

    // ✅ 모달 중복 오픈 방지
    private var onboardingDialogOpened = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentApprovedReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortBy = arguments?.getString("sortBy") ?: "recent"

        repository = TodayReservationRepository(
            RetrofitClient.getTodayReservationApi(requireContext())
        )

        // ✅ 온보딩이면 repo도 mock 사용
        val useMock = (requireActivity() as MainActivity).isOnboardingActive()
        repository.setOnboardingMode(useMock)

        adapter = TodayReservationAdapter(
            currentTab = "confirmed",
            onApprove = {},
            onReject = {},
            onChangeStatus = { item ->
                ChangeStatusDialog(item.status) { newStatus ->
                    updateStatus(item.reservationId, newStatus)
                }.show(parentFragmentManager, "change-status")
            }
        )

        binding.rvApprovedList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ApprovedReservFragment.adapter
        }

        fetchApprovedReservations()
    }

    override fun sort(sortBy: String) {
        this.sortBy = sortBy
        fetchApprovedReservations()
    }

    private fun fetchApprovedReservations() {
        lifecycleScope.launch {
            try {
                val response = repository.getTodayReservations(
                    shopId = 1L,
                    tab = "confirmed",
                    sortBy = sortBy
                )

                reservationList = response.data.reservations.toMutableList()
                adapter.submitList(reservationList.toList())

                // ✅ 리스트 갱신 뒤에 "첫 아이템의 상태변경 버튼" 잡기
                captureChangeStatusButtonFromFirstItem()

            } catch (e: Exception) {
                Log.e(TAG, "API 호출 실패", e)
            }
        }
    }

    private fun captureChangeStatusButtonFromFirstItem() {
        binding.rvApprovedList.post {
            val vh = binding.rvApprovedList.findViewHolderForAdapterPosition(0)
            if (vh == null) {
                binding.rvApprovedList.post {
                    val vh2 = binding.rvApprovedList.findViewHolderForAdapterPosition(0)
                    changeStatusBtnForGuide = vh2?.itemView?.findViewById(com.example.bisit.R.id.btnChangeStatus)
                }
                return@post
            }
            changeStatusBtnForGuide = vh.itemView.findViewById(com.example.bisit.R.id.btnChangeStatus)
        }
    }

    // ✅ TODAY_CONFIRM 단계에서 TodayReservFragment가 호출할 함수
    fun openChangeStatusDialogForOnboardingIfNeeded() {
        val activity = requireActivity() as MainActivity
        if (!activity.isOnboardingActive()) return
        if (onboardingDialogOpened) return

        // 이미 떠 있으면 또 띄우지 않기
        if (parentFragmentManager.findFragmentByTag("change-status") != null) return

        val first = reservationList.firstOrNull() ?: return

        onboardingDialogOpened = true
        ChangeStatusDialog(first.status) { newStatus ->
            updateStatus(first.reservationId, newStatus)
        }.show(parentFragmentManager, "change-status")
    }

    private fun updateStatus(reservationId: Long, newStatus: String) {
        lifecycleScope.launch {
            try {
                val body = ChangeStatusRequest(
                    targetStatus = newStatus,
                    cancellationReason = null
                )

                repository.changeStatus(reservationId, body)
                fetchApprovedReservations()

            } catch (e: Exception) {
                Log.e(TAG, "상태 변경 실패", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}