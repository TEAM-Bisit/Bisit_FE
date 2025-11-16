package com.example.bisit.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R

class MapChipAdapter(
    private val items: List<String>,
    private val onSelected: (String) -> Unit
) : RecyclerView.Adapter<MapChipAdapter.ViewHolder>() {

    private var selectedIndex = 0

    inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_map_chip, parent, false) as TextView
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = items[position]
        val tv = holder.textView

        tv.text = name

        if (position == selectedIndex) {
            tv.setBackgroundResource(R.drawable.bg_list_chip_selected)
            tv.setTextColor(Color.WHITE)
        } else {
            tv.setBackgroundResource(R.drawable.bg_list_chip_unselected)
            tv.setTextColor(Color.parseColor("#222222"))
        }

        tv.setOnClickListener {
            val adapterPos = holder.adapterPosition

            if (adapterPos == RecyclerView.NO_POSITION) return@setOnClickListener

            val oldIndex = selectedIndex
            selectedIndex = adapterPos

            notifyItemChanged(oldIndex)
            notifyItemChanged(selectedIndex)

            onSelected(items[adapterPos])
        }
    }

    override fun getItemCount(): Int = items.size
}
