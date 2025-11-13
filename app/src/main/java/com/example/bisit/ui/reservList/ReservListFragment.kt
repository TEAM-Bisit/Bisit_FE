package com.example.bisit.ui.reservList

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.databinding.FragmentReservListBinding
import com.example.bisit.ui.reservList.adapter.ReservListAdapter
import com.example.bisit.ui.reservList.dialog.ReservListCalendarDialog
import com.example.bisit.ui.reservList.model.ReservListItem
import java.text.SimpleDateFormat
import java.util.*

class ReservListFragment : Fragment() {

    private var _binding: FragmentReservListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ReservListAdapter
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    private var isAscending = true

    /** 예시 데이터 **/
    private val reservItems = listOf(
        ReservListItem(
            "1", "theheogk777", "컷트", "김ㅇㅇ",
            "2025.09.04 17:00", 25000,
            "청주시 상당구 대학로 29길", "010-0000-0000",
            "예약 확정"
        ),
        ReservListItem(
            "2", "theheogk777", "염색", "박ㅇㅇ",
            "2025.09.13 17:00", 30000,
            "청주시 흥덕구 사창동 123", "010-1111-2222",
            "예약 확인 중"
        ),
        ReservListItem(
            "3", "theheogk777", "펌", "이ㅇㅇ",
            "2025.08.15 10:00", 40000,
            "청주시 서원구 흥덕로 77", "010-2222-3333",
            "예약 확정"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** RecyclerView 기본 설정 **/
        adapter = ReservListAdapter(reservItems) { selected ->
            val bundle = Bundle().apply {
                putSerializable("reservItem", selected)
            }
            findNavController().navigate(R.id.action_reservList_to_detail, bundle)
        }

        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter

        /** 달력 버튼 **/
        binding.btnCalendar.setOnClickListener {
            ReservListCalendarDialog { selectedDate ->
                // TODO: 선택된 날짜 필터링 구현
            }.show(childFragmentManager, "CalendarDialog")
        }

        /** 정렬 버튼 클릭 이벤트 **/
        binding.btnAscending.setOnClickListener {
            if (!isAscending) {
                isAscending = true
                updateButtonUI()
                sortList()
            }
        }

        binding.btnDescending.setOnClickListener {
            if (isAscending) {
                isAscending = false
                updateButtonUI()
                sortList()
            }
        }

        /** 기본 오름차순 정렬 **/
        sortList()
        updateButtonUI()
    }

    /** 리스트 정렬 함수 **/
    private fun sortList() {
        val sortedList = if (isAscending) {
            reservItems.sortedBy { dateFormat.parse(it.dateTime) }
        } else {
            reservItems.sortedByDescending { dateFormat.parse(it.dateTime) }
        }
        adapter.updateList(sortedList)
    }

    /** 버튼 색상 및 배경 변경 **/
    private fun updateButtonUI() {
        if (isAscending) {
            // 오름차순 활성화
            binding.btnAscending.setBackgroundResource(R.drawable.bg_order_active)
            binding.btnAscending.setTextColor(Color.WHITE)

            // 내림차순 비활성화
            binding.btnDescending.setBackgroundResource(R.drawable.bg_order_inactive)
            binding.btnDescending.setTextColor("#222222".toColorInt())
        } else {
            binding.btnDescending.setBackgroundResource(R.drawable.bg_order_active)
            binding.btnDescending.setTextColor(Color.WHITE)

            binding.btnAscending.setBackgroundResource(R.drawable.bg_order_inactive)
            binding.btnAscending.setTextColor("#222222".toColorInt())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
