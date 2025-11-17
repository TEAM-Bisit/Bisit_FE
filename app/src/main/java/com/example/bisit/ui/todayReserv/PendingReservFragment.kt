package com.example.bisit.ui.todayReserv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentPendingReservBinding
import com.example.bisit.data.model.todayReservation.ReservationItem
import com.example.bisit.ui.todayReserv.adapter.TodayReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ApproveCompleteDialog
import com.example.bisit.ui.todayReserv.dialog.ChangeReasonDialog

class PendingReservFragment : Fragment(), SortableFragment {

    private var _binding: FragmentPendingReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var pendingList = mutableListOf<ReservationItem>()

    private var sortBy: String = "recent"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sortBy = arguments?.getString("sortBy") ?: "recent"

        pendingList = mutableListOf(
            ReservationItem(
                reservationId = 1L,
                status = "PENDING",
                serviceStatus = "NONE",
                customerName = "김철수",
                treatmentName = "볼륨 매직",
                staffName = "박디자이너",
                visitAddressLine = "서울 강남구 논현로 123",
                reservedDate = "2025-10-07",
                startTime = "13:00"
            ),
            ReservationItem(
                reservationId = 2L,
                status = "PENDING",
                serviceStatus = "NONE",
                customerName = "이영희",
                treatmentName = "염색 & 클리닉",
                staffName = "김디자이너",
                visitAddressLine = "서울 마포구 연남로 22길 8",
                reservedDate = "2025-10-08",
                startTime = "10:30"
            )
        )

        adapter = TodayReservationAdapter(
            currentTab = "pending",
            onApprove = { item ->
                ApproveCompleteDialog().show(parentFragmentManager, "approve_dialog")

                pendingList.remove(item)
                adapter.submitList(pendingList.toList())
            },
            onReject = { item ->

                ChangeReasonDialog(
                    onRejectConfirmed = {
                        pendingList.remove(item)
                        adapter.submitList(pendingList.toList())
                    }
                ).show(parentFragmentManager, "reject_dialog")
            },
            onChangeStatus = {}
        )

        binding.rvPendingList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingReservFragment.adapter
        }

        sort(sortBy)
    }

    override fun sort(sortBy: String) {
        this.sortBy = sortBy

        val sorted = when (sortBy) {
            "recent" -> pendingList.sortedByDescending { "${it.reservedDate} ${it.startTime}" }
            "oldest" -> pendingList.sortedBy { "${it.reservedDate} ${it.startTime}" }
            else -> pendingList
        }

        pendingList = sorted.toMutableList()
        adapter.submitList(pendingList.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
