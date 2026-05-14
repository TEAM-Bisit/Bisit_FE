package kr.bisit.app.ui.todayReserv.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R
import kr.bisit.app.data.model.todayReservation.ReservationItem
import kr.bisit.app.databinding.ItemApprovedReservationBinding

class TodayReservationAdapter(
    private val currentTab: String,   // "pending" 또는 "confirmed"
    private val onApprove: (ReservationItem) -> Unit,
    private val onReject: (ReservationItem) -> Unit,
    private val onChangeStatus: (ReservationItem) -> Unit
) : ListAdapter<ReservationItem, TodayReservationAdapter.ViewHolder>(ReservationDiffCallback()) {

    inner class ViewHolder(
        private val binding: ItemApprovedReservationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ReservationItem) = with(binding) {

            // ---------------------------------------------------------
            // 재사용 대비 초기화
            // ---------------------------------------------------------
            root.visibility = View.VISIBLE
            tvStatus.visibility = View.VISIBLE
            tvReservationNumber.visibility = View.VISIBLE
            tvReservationId.visibility = View.VISIBLE
            spacer.visibility = View.VISIBLE

            layoutPendingButtons.visibility = View.GONE
            btnApprove.visibility = View.GONE
            btnReject.visibility = View.GONE
            btnChangeStatus.visibility = View.GONE

            // 상태 변환 텍스트
            val realStatus = item.status.trim().uppercase()
            val statusText = when (realStatus) {
                "PENDING" -> "확정 대기"
                "CONFIRMED", "CUSTOMER_CONFIRMED" -> "예약 확정"
                "CANCELED_BY_CUSTOMER", "CANCELED_BY_SHOP" -> "취소"
                "COMPLETED" -> "시술 완료"
                "NO_SHOW" -> "노쇼"
                else -> item.status
            }

            // ---------------------------------------------------------
            // Pending 탭 → 승인/거절 UI
            // ---------------------------------------------------------
            if (currentTab == "pending") {

                // 상태/예약번호 숨김
                tvStatus.visibility = View.GONE
                tvReservationNumber.visibility = View.GONE
                tvReservationId.visibility = View.GONE
                spacer.visibility = View.GONE

                // 부모 레이아웃 보이기
                layoutPendingButtons.visibility = View.VISIBLE

                // 버튼 표시
                btnApprove.visibility = View.VISIBLE
                btnReject.visibility = View.VISIBLE

                btnApprove.setOnClickListener { onApprove(item) }
                btnReject.setOnClickListener { onReject(item) }

            } else {

                // ---------------------------------------------------------
                // Confirmed 탭 → 상태 표시 + 변경 버튼
                // ---------------------------------------------------------
                tvStatus.visibility = View.VISIBLE
                tvStatus.text = statusText

                btnChangeStatus.visibility = View.VISIBLE
                btnChangeStatus.setOnClickListener { onChangeStatus(item) }

                // 상태 색상
                val statusColor = when (realStatus) {
                    "CONFIRMED", "CUSTOMER_CONFIRMED" -> R.color.blue_4076FF
                    "NO_SHOW" -> R.color.red_F54343
                    else -> R.color.gray_515965
                }
                tvStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

                // Pending 전용 버튼 레이아웃은 숨김
                layoutPendingButtons.visibility = View.GONE
            }

            // ---------------------------------------------------------
            // 공통 데이터 바인딩
            // ---------------------------------------------------------
            tvReservationId.text = item.reservationId.toString()
            tvShopName.text = item.treatmentName
            tvDate.text = "${item.reservedDate} ${item.startTime}"
            tvName.text = item.customerName
            tvAddress.text = item.visitAddressLine
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemApprovedReservationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ReservationDiffCallback : DiffUtil.ItemCallback<ReservationItem>() {
    override fun areItemsTheSame(oldItem: ReservationItem, newItem: ReservationItem): Boolean {
        return oldItem.reservationId == newItem.reservationId
    }

    override fun areContentsTheSame(oldItem: ReservationItem, newItem: ReservationItem): Boolean {
        return oldItem == newItem
    }
}
