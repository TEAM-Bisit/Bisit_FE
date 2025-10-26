package com.example.bisit.ui.todayReserv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentApprovedReservBinding
import com.example.bisit.ui.todayReserv.adapter.ApprovedReservation
import com.example.bisit.ui.todayReserv.adapter.ApprovedReservationAdapter
import com.example.bisit.ui.todayReserv.dialog.ChangeStatusDialog

class ApprovedReservFragment : Fragment(), SortableFragment {
    private var _binding: FragmentApprovedReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ApprovedReservationAdapter
    private var originalList = mutableListOf<ApprovedReservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApprovedReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalList = mutableListOf(
            ApprovedReservation(
                reservationId = "fhEheoqkr777",
                shopName = "컷트",
                date = "2025.09.04 17:00",
                address = "경산시 하양읍 대학로 298길 20-9 롯데아파트 101동 111호",
                status = "예약 확정"
            ),
            ApprovedReservation(
                reservationId = "fhEheoqkr778",
                shopName = "헤드 스파",
                date = "2025.09.10 14:00",
                address = "서울특별시 마포구 서교동 12길 15",
                status = "확정 대기"
            ),
            ApprovedReservation(
                reservationId = "fhEheoqkr779",
                shopName = "염색 & 클리닉",
                date = "2025.09.12 10:30",
                address = "부산광역시 해운대구 달맞이길 54",
                status = "시술 완료"
            ),
            ApprovedReservation(
                reservationId = "fhEheoqkr780",
                shopName = "볼륨 매직",
                date = "2025.09.15 13:00",
                address = "서울특별시 강남구 논현로 45길 12",
                status = "노쇼"
            ),
            ApprovedReservation(
                reservationId = "fhEheoqkr781",
                shopName = "클리닉 & 드라이",
                date = "2025.09.18 15:00",
                address = "대전광역시 유성구 문화로 102번길 9",
                status = "취소"
            )
        )

        adapter = ApprovedReservationAdapter { item ->
            ChangeStatusDialog(item.status) { newStatus ->
                val itemToUpdate = originalList.find { it.reservationId == item.reservationId }
                itemToUpdate?.status = newStatus
                adapter.submitList(originalList.toList())
            }.show(parentFragmentManager, "change_status")
        }

        binding.rvApprovedList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ApprovedReservFragment.adapter
        }

        adapter.submitList(originalList)
    }

    override fun sort(isRecent: Boolean) {
        val sortedList = if (isRecent) {
            originalList.sortedByDescending { it.date }
        } else {
            originalList.sortedBy { it.date }
        }
        adapter.submitList(sortedList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
