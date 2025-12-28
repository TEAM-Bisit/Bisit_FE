package com.example.bisit.ui.customerReserve

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.databinding.ItemCalendarDayBinding
import java.util.*

class CalendarAdapter(
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayViewHolder>() {

    private var days = listOf<Calendar?>()
    private var selectedDate: Calendar? = null
    private var today: Calendar = Calendar.getInstance()

    fun submitList(newDays: List<Calendar?>) {
        days = newDays
        notifyDataSetChanged()
    }

    fun setSelectedDate(date: Calendar?) {
        selectedDate = date
        notifyDataSetChanged()
    }

    private var closedDays: Set<Int> = emptySet()

    fun setClosedDays(days: Set<Int>) {
        closedDays = days
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(days[position])
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: Calendar?) {
            if (date == null) {
                binding.root.visibility = View.INVISIBLE
                return
            }
            binding.root.visibility = View.VISIBLE

            val dayOfMonth = date.get(Calendar.DAY_OF_MONTH)
            binding.tvDay.text = dayOfMonth.toString()

            // Check if it's today
            val isToday = isSameDay(date, today)
            binding.tvToday.visibility = if (isToday) View.VISIBLE else View.GONE

            // Check if it's past date
            val isPast = isBeforeDay(date, today)
            
            // Check if it's a closed day
            val dayOfWeek = date.get(Calendar.DAY_OF_WEEK)
            val isClosed = closedDays.contains(dayOfWeek)

            // Check selection
            val isSelected = selectedDate != null && isSameDay(date, selectedDate!!)
            
            if (isPast || isClosed) {
                binding.viewSelection.visibility = View.INVISIBLE
                binding.tvDay.setTextColor(Color.parseColor("#DDDDDD")) // Gray for disabled
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
                
                // Optional: visual indicator for closed? For now just gray out like past dates.
            } else {
                binding.root.isClickable = true
                if (isSelected) {
                    binding.viewSelection.visibility = View.VISIBLE
                    binding.tvDay.setTextColor(Color.WHITE)
                } else {
                    binding.viewSelection.visibility = View.INVISIBLE
                    
                    // Text color based on day of week or today
                    when {
                        isToday -> binding.tvDay.setTextColor(Color.parseColor("#4076FF"))
                        date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> binding.tvDay.setTextColor(Color.parseColor("#FF6B6B"))
                        else -> binding.tvDay.setTextColor(Color.parseColor("#222222"))
                    }
                }

                binding.root.setOnClickListener {
                    onDateSelected(date)
                }
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }

        private fun isBeforeDay(cal1: Calendar, cal2: Calendar): Boolean {
            if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true
            if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) return false
            
            if (cal1.get(Calendar.MONTH) < cal2.get(Calendar.MONTH)) return true
            if (cal1.get(Calendar.MONTH) > cal2.get(Calendar.MONTH)) return false
            
            return cal1.get(Calendar.DAY_OF_MONTH) < cal2.get(Calendar.DAY_OF_MONTH)
        }
    }
}
