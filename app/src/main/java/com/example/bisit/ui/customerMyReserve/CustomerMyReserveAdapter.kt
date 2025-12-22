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
