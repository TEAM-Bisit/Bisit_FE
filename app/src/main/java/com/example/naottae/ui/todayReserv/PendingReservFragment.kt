package com.example.naottae.ui.todayReserv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.naottae.databinding.FragmentPendingReservBinding
import com.example.naottae.ui.todayReserv.adapter.Reservation
import com.example.naottae.ui.todayReserv.adapter.ReservationAdapter
import com.example.naottae.ui.todayReserv.dialog.ApproveCompleteDialog
import com.example.naottae.ui.todayReserv.dialog.ChangeReasonDialog

class PendingReservFragment : Fragment(), SortableFragment {
    private var _binding: FragmentPendingReservBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReservationAdapter
    private var originalList = mutableListOf<Reservation>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingReservBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        originalList = mutableListOf(
            Reservation("id1", "볼륨 매직", "2025.10.07 13:00", "서울 강남구 논현로 45길 12"),
            Reservation("id2", "염색 & 클리닉", "2025.10.08 10:30", "서울 마포구 연남로 22길 8"),
            Reservation("id3", "컷트 & 스타일링", "2025.10.09 17:20", "서울 서초구 서초대로 78길 4"),
            Reservation("id4", "헤드 스파", "2025.10.10 14:00", "서울 용산구 한남대로 12길 21"),
            Reservation("id5", "남성 커트", "2025.10.11 11:10", "서울 송파구 백제고분로 33길 19")
        )

        adapter = ReservationAdapter(
            onReject = { item ->
                ChangeReasonDialog().show(parentFragmentManager, "reject")
                originalList.remove(item)
                adapter.submitList(originalList.toList())
            },
            onApprove = { item ->
                ApproveCompleteDialog().show(parentFragmentManager, "approve")
                originalList.remove(item)
                adapter.submitList(originalList.toList())
            }
        )

        binding.rvPendingList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PendingReservFragment.adapter
        }

        adapter.submitList(originalList.toList())
    }


    override fun sort(isRecent: Boolean) {
        val sortedList = if (isRecent) {
            originalList.sortedByDescending { it.date }
        } else {
            originalList.sortedBy { it.date }
        }
        originalList = sortedList.toMutableList()
        adapter.submitList(originalList.toList())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
