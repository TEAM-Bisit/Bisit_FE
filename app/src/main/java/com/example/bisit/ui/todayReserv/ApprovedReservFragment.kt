package com.example.bisit.ui.todayReserv

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.todayReservation.ChangeStatusRequest
import com.example.bisit.data.model.todayReservation.ReservationItem
import com.example.bisit.data.repository.todayReservation.TodayReservationRepository
import com.example.bisit.databinding.FragmentApprovedReservBinding
import com.example.bisit.ui.todayReserv.adapter.TodayReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ChangeStatusDialog
import kotlinx.coroutines.launch

class ApprovedReservFragment : Fragment(), SortableFragment {

    companion object {
        private const val TAG = "ApprovedReserv"
    }

    private var _binding: FragmentApprovedReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var reservationList = mutableListOf<ReservationItem>()

    private var sortBy: String = "recent"

    // ✅ Repository 사용
    private lateinit var repository: TodayReservationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovedReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "Fragment 진입")

        sortBy = arguments?.getString("sortBy") ?: "recent"
        Log.d(TAG, "초기 sortBy: $sortBy")

        // ✅ Repository 초기화
        repository = TodayReservationRepository(
            RetrofitClient.getTodayReservationApi(requireContext())
        )

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

        // 최초 진입 시 API 호출
        fetchApprovedReservations()
    }

    /**
     * 🔹 정렬 변경 시 호출
     */
    override fun sort(sortBy: String) {
        this.sortBy = sortBy
        Log.d(TAG, "정렬 변경 요청: $sortBy")
        fetchApprovedReservations()
    }

    /**
     * 🔹 승인/완료 예약 조회 API
     */
    private fun fetchApprovedReservations() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "API 호출 시작")

                val response = repository.getTodayReservations(
                    shopId = 1L,        // TODO 실제 shopId
                    tab = "confirmed", // 서버 명세 확인 필수
                    sortBy = sortBy
                )

                Log.d(TAG, "API 성공")
                Log.d(TAG, "응답 data: ${response.data}")

                val reservations = response.data.reservations
                reservationList = reservations.toMutableList()

                adapter.submitList(reservationList.toList())

                Log.d(TAG, "RecyclerView 갱신 완료 (size=${reservationList.size})")

            } catch (e: Exception) {
                Log.e(TAG, "API 호출 실패", e)
            }
        }
    }

    private fun updateStatus(reservationId: Long, newStatus: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "상태 변경 요청: id=$reservationId → $newStatus")

                val body = ChangeStatusRequest(
                    targetStatus = newStatus,
                    cancellationReason = null
                )

                val response = repository.changeStatus(
                    reservationId = reservationId,
                    body = body
                )

                Log.d(TAG, "상태 변경 성공: ${response.data}")

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
