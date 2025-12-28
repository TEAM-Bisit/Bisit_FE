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
import com.example.bisit.data.model.todayReservation.RejectReservationRequest
import com.example.bisit.data.model.todayReservation.ReservationItem
import com.example.bisit.data.repository.todayReservation.TodayReservationRepository
import com.example.bisit.databinding.FragmentPendingReservBinding
import com.example.bisit.ui.todayReserv.adapter.TodayReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ApproveCompleteDialog
import com.example.bisit.ui.todayReserv.dialog.ChangeReasonDialog
import kotlinx.coroutines.launch

class PendingReservFragment : Fragment(), SortableFragment {

    companion object {
        private const val TAG = "PendingReserv"
    }

    private var _binding: FragmentPendingReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var pendingList = mutableListOf<ReservationItem>()

    private var sortBy: String = "recent"

    private lateinit var repository: TodayReservationRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "Fragment 진입")

        sortBy = arguments?.getString("sortBy") ?: "recent"
        Log.d(TAG, "초기 sortBy: $sortBy")

        // Repository 초기화
        repository = TodayReservationRepository(
            RetrofitClient.getTodayReservationApi(requireContext())
        )

        adapter = TodayReservationAdapter(
            currentTab = "pending",

            // ✅ 승인
            onApprove = { item ->
                lifecycleScope.launch {
                    try {
                        Log.d(TAG, "승인 요청: id=${item.reservationId}")

                        repository.approveReservation(item.reservationId)

                        Log.d(TAG, "승인 성공")

                        ApproveCompleteDialog().show(
                            parentFragmentManager,
                            "approve_complete"
                        )

                        // 승인 후 다시 조회
                        fetchPendingReservations()

                    } catch (e: Exception) {
                        Log.e(TAG, "승인 실패", e)
                    }
                }
            },

            // ✅ 거절 (사유 포함)
            onReject = { item ->
                ChangeReasonDialog { reason ->
                    lifecycleScope.launch {
                        try {
                            Log.d(
                                TAG,
                                "거절 요청: id=${item.reservationId}, reason=$reason"
                            )

                            repository.rejectReservation(
                                reservationId = item.reservationId,
                                body = RejectReservationRequest(
                                    rejectionReason = reason
                                )
                            )

                            Log.d(TAG, "거절 성공")

                            // 거절 후 다시 조회
                            fetchPendingReservations()

                        } catch (e: Exception) {
                            Log.e(TAG, "거절 실패", e)
                        }
                    }
                }.show(parentFragmentManager, "reject_reason")
            },

            onChangeStatus = {}
        )

        binding.rvPendingList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingReservFragment.adapter
        }

        // 최초 진입 시 조회
        fetchPendingReservations()
    }

    /**
     * 정렬 변경 시 호출
     */
    override fun sort(sortBy: String) {
        this.sortBy = sortBy
        Log.d(TAG, "정렬 변경 요청: $sortBy")
        fetchPendingReservations()
    }

    /**
     * Pending 예약 조회 API
     */
    private fun fetchPendingReservations() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Pending 예약 조회 API 호출")

                val response = repository.getTodayReservations(
                    shopId = 1L,   // TODO 실제 shopId
                    tab = "pending",
                    sortBy = sortBy
                )

                Log.d(TAG, "조회 성공: ${response.data}")

                val reservations = response.data.reservations
                pendingList = reservations.toMutableList()

                adapter.submitList(pendingList.toList())

                Log.d(
                    TAG,
                    "RecyclerView 갱신 완료 (size=${pendingList.size})"
                )

            } catch (e: Exception) {
                Log.e(TAG, "조회 실패", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
