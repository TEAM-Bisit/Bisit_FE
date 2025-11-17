package com.example.bisit.ui.todayReserv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.data.model.todayReservation.ReservationItem
import com.example.bisit.databinding.FragmentApprovedReservBinding
import com.example.bisit.ui.todayReserv.adapter.TodayReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ChangeStatusDialog

class ApprovedReservFragment : Fragment(), SortableFragment {

    private var _binding: FragmentApprovedReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TodayReservationAdapter
    private var reservationList = mutableListOf<ReservationItem>()

    // TodayReservFragment에서 넘겨주는 정렬값
    private var sortBy: String = "recent"

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

        // arguments에서 정렬값 가져오기
        sortBy = arguments?.getString("sortBy") ?: "recent"

        val currentTab = "confirmed"

        // --------------------------
        // 목데이터
        // --------------------------
        reservationList = mutableListOf(
            ReservationItem(
                reservationId = 1,
                status = "CONFIRMED",
                serviceStatus = "NONE",
                customerName = "김철수",
                treatmentName = "컷트",
                staffName = "원장",
                visitAddressLine = "서울시 강남구 논현로 10길 12",
                reservedDate = "2025-09-04",
                startTime = "17:00"
            ),
            ReservationItem(
                reservationId = 2,
                status = "PENDING",   // confirmed 탭에서도 보임
                serviceStatus = "NONE",
                customerName = "박민서",
                treatmentName = "헤드 스파",
                staffName = "실장",
                visitAddressLine = "서울시 마포구 연남로 22길 8",
                reservedDate = "2025-09-10",
                startTime = "14:00"
            ),
            ReservationItem(
                reservationId = 3,
                status = "COMPLETED",
                serviceStatus = "NONE",
                customerName = "이현지",
                treatmentName = "클리닉",
                staffName = "디자이너",
                visitAddressLine = "부산 해운대구 달맞이길 24",
                reservedDate = "2025-09-12",
                startTime = "10:30"
            )
        )

        adapter = TodayReservationAdapter(
            currentTab = currentTab,
            onApprove = {},
            onReject = {},
            onChangeStatus = { item ->
                ChangeStatusDialog(item.status) { newStatus ->
                    val idx =
                        reservationList.indexOfFirst { it.reservationId == item.reservationId }

                    if (idx != -1) {
                        reservationList[idx] = reservationList[idx].copy(status = newStatus)
                    }

                    // 변경 후 정렬 반영
                    sort(sortBy)
                }.show(parentFragmentManager, "change-status")
            }
        )

        binding.rvApprovedList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ApprovedReservFragment.adapter
        }

        // 초기 정렬 반영 후 submit
        sort(sortBy)
    }

    override fun sort(sortBy: String) {
        this.sortBy = sortBy

        val sorted = when (sortBy) {
            "recent" -> reservationList.sortedByDescending { "${it.reservedDate} ${it.startTime}" }
            "oldest" -> reservationList.sortedBy { "${it.reservedDate} ${it.startTime}" }
            else -> reservationList
        }

        adapter.submitList(sorted)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
