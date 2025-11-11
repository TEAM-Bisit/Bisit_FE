package com.example.bisit.ui.customerMyReserve

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CustomerMyReserveAdapter : RecyclerView.Adapter<CustomerMyReserveAdapter.ViewHolder>() {

    private var items: List<Int> = listOf()
    private var itemCount = 5

    fun setItems(layoutRes: Int) {
        items = List(itemCount) { layoutRes }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position]
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
