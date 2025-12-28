package com.example.bisit.ui.customerMyReserve

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

class CustomerMyReserveAdapter(
    private val onDetailClick: (MyReserveItem) -> Unit
) : RecyclerView.Adapter<CustomerMyReserveAdapter.ViewHolder>() {

    private var items: List<MyReserveItem> = listOf()

    // 데이터 설정
    fun setItems(newItems: List<MyReserveItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, onDetailClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        // Return layout resource based on status for now, or use the one passed previously?
        // To query simple, I'll map status to layout R.
        return when (items[position].status) {
            "예약" -> R.layout.item_customer_my_reserve_wait
            "완료" -> R.layout.item_customer_my_reserve_completed
            "취소" -> R.layout.item_customer_my_reserve_canceled
            else -> R.layout.item_customer_my_reserve_wait
        }
    }

    class ViewHolder(itemView: View, private val onDetailClick: (MyReserveItem) -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MyReserveItem) {
            val context = itemView.context
            
            // Bind common fields
            itemView.findViewById<TextView>(R.id.tv_wait_reserv_no)?.text = "예약 번호  ${item.orderId ?: item.reservationId}"
            itemView.findViewById<TextView>(R.id.tv_wait_shop)?.text = item.shopName
            itemView.findViewById<TextView>(R.id.tv_wait_datetime)?.text = formatDateTime(item.reservedDate)
            itemView.findViewById<TextView>(R.id.tv_wait_service)?.text = "${item.treatmentName}  ${formatPrice(item.price)}원"
            
            // For completed layout
            itemView.findViewById<TextView>(R.id.tv_done_reserv_no)?.text = "예약 번호  ${item.orderId ?: item.reservationId}"
            itemView.findViewById<TextView>(R.id.tv_done_shop)?.text = item.shopName
            itemView.findViewById<TextView>(R.id.tv_done_datetime)?.text = formatDateTime(item.reservedDate)
            itemView.findViewById<TextView>(R.id.tv_done_service)?.text = "${item.treatmentName}  ${formatPrice(item.price)}원"
            
            // For canceled layout
            itemView.findViewById<TextView>(R.id.tv_cancel_reserv_no)?.text = "예약 번호  ${item.orderId ?: item.reservationId}"
            itemView.findViewById<TextView>(R.id.tv_cancel_shop)?.text = item.shopName
            itemView.findViewById<TextView>(R.id.tv_cancel_datetime)?.text = formatDateTime(item.reservedDate)
            itemView.findViewById<TextView>(R.id.tv_cancel_service)?.text = "${item.treatmentName}  ${formatPrice(item.price)}원"
            
            val btnDoneDetail = itemView.findViewById<Button>(R.id.btn_done_detail)
            btnDoneDetail?.setOnClickListener {
                onDetailClick(item)
            }

            val btnCancelDetail = itemView.findViewById<Button>(R.id.btn_cancel_detail)
            btnCancelDetail?.setOnClickListener {
                onDetailClick(item)
            }

            val btnInquire = itemView.findViewById<Button>(R.id.btn_wait_inquire)
            btnInquire?.setOnClickListener {
                showInquireDialog(context)
            }
        }
        
        private fun formatDateTime(dateStr: String): String {
            return try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                if (date != null) outputFormat.format(date) else dateStr
            } catch (e: Exception) {
                dateStr
            }
        }
        
        private fun formatPrice(price: Int): String {
            return java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(price)
        }

        private fun showInquireDialog(context: Context) {
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_customer_my_reserve_ask, null)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            dialogView.findViewById<TextView>(R.id.btnClose)?.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }
}
