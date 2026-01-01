package com.example.bisit.ui.reservList

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.R
import com.example.bisit.databinding.FragmentReservListBinding
import com.example.bisit.ui.reservList.adapter.ReservListAdapter
import com.example.bisit.ui.reservList.dialog.ReservListCalendarDialog
import com.example.bisit.ui.shop.ShopRegisterViewModel
import com.example.bisit.ui.shop.ShopRegisterViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReservListFragment : Fragment() {

    private var _binding: FragmentReservListBinding? = null
    private val binding get() = _binding!!

    /** 예약 리스트 ViewModel */
    private val reservListViewModel: ReservListViewModel by viewModels()

    /** shopId 공유 ViewModel (Activity 범위) */
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    private lateinit var adapter: ReservListAdapter
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
    private var isAscending = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReservListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** RecyclerView 설정 */
        adapter = ReservListAdapter(emptyList()) { selected ->
            val bundle = Bundle().apply {
                putLong("reservationId", selected.reservationId)
            }
            findNavController().navigate(
                R.id.action_reservList_to_detail,
                bundle
            )
        }

        binding.rvReservations.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReservations.adapter = adapter

        observeShopId()
        observeReservationList()

        /** 달력 버튼 */
        binding.btnCalendar.setOnClickListener {
            ReservListCalendarDialog { selectedDate ->
                // 날짜 필터 적용 시
                // reservListViewModel.loadReservationList(
                //     shopId = currentShopId,
                //     date = selectedDate,
                //     isRefresh = true
                // )
            }.show(childFragmentManager, "CalendarDialog")
        }

        /** 정렬 버튼 (기존 로직 그대로) */
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

        updateButtonUI()
    }

    /**
     * shopId 수신 → 예약 목록 조회
     */
    private fun observeShopId() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                shopRegisterViewModel.shopId.collect { shopId ->
                    shopId ?: return@collect

                    reservListViewModel.loadReservationList(
                        shopId = shopId,
                        isRefresh = true
                    )
                }
            }
        }
    }

    /**
     * 예약 목록 관찰
     */
    private fun observeReservationList() {
        reservListViewModel.reservationList.observe(viewLifecycleOwner) { list ->
            sortAndSubmit(list)
        }
    }

    /** 기존 정렬 로직 유지 */
    private fun sortList() {
        reservListViewModel.reservationList.value?.let {
            sortAndSubmit(it)
        }
    }

    private fun sortAndSubmit(
        list: List<com.example.bisit.data.model.reservList.ReservationListItem>
    ) {
        val sortedList = if (isAscending) {
            list.sortedBy {
                dateFormat.parse("${it.reservedDate} ${it.startTime}")
            }
        } else {
            list.sortedByDescending {
                dateFormat.parse("${it.reservedDate} ${it.startTime}")
            }
        }
        adapter.updateList(sortedList)
    }

    /** 버튼 UI (기존 그대로) */
    private fun updateButtonUI() {
        if (isAscending) {
            binding.btnAscending.setBackgroundResource(R.drawable.bg_order_active)
            binding.btnAscending.setTextColor(Color.WHITE)

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
