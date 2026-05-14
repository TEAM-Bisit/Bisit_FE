package kr.bisit.app.ui.todayReserv

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.todayReservation.RejectReservationRequest
import kr.bisit.app.data.model.todayReservation.ReservationItem
import kr.bisit.app.data.repository.todayReservation.TodayReservationRepository
import kr.bisit.app.databinding.FragmentPendingReservBinding
import kr.bisit.app.ui.todayReserv.adapter.TodayReservationAdapter
import kr.bisit.app.ui.todayReserv.dialog.ApproveCompleteDialog
import kr.bisit.app.ui.todayReserv.dialog.ChangeReasonDialog
import kotlinx.coroutines.launch

class PendingReservFragment : Fragment(), SortableFragment, TodayApproveTargetProvider {

    companion object { private const val TAG = "PendingReserv" }

    private var _binding: FragmentPendingReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var pendingList = mutableListOf<ReservationItem>()

    private var sortBy: String = "recent"
    private lateinit var repository: TodayReservationRepository

    private var approveBtnForGuide: View? = null
    override fun getApproveButtonForGuide(): View? = approveBtnForGuide

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

        val activity = requireActivity() as kr.bisit.app.MainActivity
        val useMock = activity.isOnboardingActive()

        repository = TodayReservationRepository(
            RetrofitClient.getTodayReservationApi(requireContext()),
        )

        repository.setOnboardingMode(useMock)

        adapter = TodayReservationAdapter(
            currentTab = "pending",

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

                        fetchPendingReservations()
                    } catch (e: Exception) {
                        Log.e(TAG, "승인 실패", e)
                    }
                }
            },

            onReject = { item ->
                ChangeReasonDialog { reason ->
                    lifecycleScope.launch {
                        try {
                            Log.d(TAG, "거절 요청: id=${item.reservationId}, reason=$reason")

                            repository.rejectReservation(
                                reservationId = item.reservationId,
                                body = RejectReservationRequest(rejectionReason = reason)
                            )

                            Log.d(TAG, "거절 성공")
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

        fetchPendingReservations()
    }

    override fun sort(sortBy: String) {
        this.sortBy = sortBy
        Log.d(TAG, "정렬 변경 요청: $sortBy")
        fetchPendingReservations()
    }

    private fun fetchPendingReservations() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Pending 예약 조회 API 호출")

                val response = repository.getTodayReservations(
                    shopId = 1L,
                    tab = "pending",
                    sortBy = sortBy
                )

                val reservations = response.data.reservations
                pendingList = reservations.toMutableList()
                adapter.submitList(pendingList.toList()) {
                    binding.rvPendingList.post {
                        binding.rvPendingList.scrollToPosition(0)

                        captureApproveButtonFromFirstItem()
                    }
                }

                Log.d(TAG, "RecyclerView 갱신 완료 (size=${pendingList.size})")

                captureApproveButtonFromFirstItem()

            } catch (e: Exception) {
                Log.e(TAG, "조회 실패", e)
            }
        }
    }

    private fun captureApproveButtonFromFirstItem() {
        binding.rvPendingList.post {
            val vh = binding.rvPendingList.findViewHolderForAdapterPosition(0)
            approveBtnForGuide = vh?.itemView?.findViewById(kr.bisit.app.R.id.btnApprove)

            // 그래도 null이면(진짜 극초반) 한번만 더 늦춰서 재시도
            if (approveBtnForGuide == null) {
                binding.rvPendingList.post {
                    val vh2 = binding.rvPendingList.findViewHolderForAdapterPosition(0)
                    approveBtnForGuide = vh2?.itemView?.findViewById(kr.bisit.app.R.id.btnApprove)
                    Log.d(TAG, "approveBtnForGuide(retry)=$approveBtnForGuide")
                }
            } else {
                Log.d(TAG, "approveBtnForGuide=$approveBtnForGuide")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}