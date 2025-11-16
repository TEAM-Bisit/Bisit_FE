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
    private val onDetailClick: () -> Unit
) : RecyclerView.Adapter<CustomerMyReserveAdapter.ViewHolder>() {

    private var items: List<Int> = listOf()
    private var itemCount = 5 // 각 탭에서 보여줄 아이템 수

    // 레이아웃 리소스 설정
    fun setItems(layoutRes: Int) {
        items = List(itemCount) { layoutRes }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view, onDetailClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(holder.itemView.context)
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = items[position]

    class ViewHolder(itemView: View, private val onDetailClick: () -> Unit) : RecyclerView.ViewHolder(itemView) {

        fun bind(context: Context) {
            val btnDoneDetail = itemView.findViewById<Button>(R.id.btn_done_detail)
            btnDoneDetail?.setOnClickListener {
                onDetailClick()
            }

            val btnCancelDetail = itemView.findViewById<Button>(R.id.btn_cancel_detail)
            btnCancelDetail?.setOnClickListener {
                onDetailClick()
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
