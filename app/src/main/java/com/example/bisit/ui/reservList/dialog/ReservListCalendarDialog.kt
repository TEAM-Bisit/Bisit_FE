package com.example.bisit.ui.reservList.dialog

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import com.example.bisit.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReservListCalendarDialog(
    private val onDateSelected: (Calendar) -> Unit
) : DialogFragment() {

    private val calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)
    private val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Seoul"), Locale.KOREA)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_reserv_list_calendar)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setDimAmount(0.8f)

        val metrics = resources.displayMetrics
        val width = (metrics.widthPixels * 0.8f).toInt()
        dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

        val btnPrev = dialog.findViewById<ImageButton>(R.id.btnPrev)
        val btnNext = dialog.findViewById<ImageButton>(R.id.btnNext)
        val tvMonth = dialog.findViewById<TextView>(R.id.tvMonth)
        val gridDaysOfWeek = dialog.findViewById<GridLayout>(R.id.gridDaysOfWeek)
        val gridCalendar = dialog.findViewById<GridLayout>(R.id.gridCalendar)

        setupDaysOfWeek(gridDaysOfWeek)

        fun updateMonth() {
            val format = SimpleDateFormat("yy.MM", Locale.KOREA)
            tvMonth.text = format.format(calendar.time)
            drawCalendar(gridCalendar)
        }

        btnPrev.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonth()
        }

        btnNext.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonth()
        }

        updateMonth()
        return dialog
    }

    /** 상단 요일 헤더 */
    private fun setupDaysOfWeek(grid: GridLayout) {
        grid.removeAllViews()
        val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")

        for (day in daysOfWeek) {
            val tv = TextView(requireContext()).apply {
                text = day
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, 10)
                setTextColor(
                    if (day == "일") "#FE6B6B".toColorInt()
                    else "#222222".toColorInt()
                )
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }
            grid.addView(tv)
        }
    }

    /** 달력 전체 날짜 렌더링 */
    private fun drawCalendar(grid: GridLayout) {
        grid.removeAllViews()
        grid.columnCount = 7

        val curMonth = calendar.clone() as Calendar

        // 이번 달의 시작 요일
        val firstDayOfWeek = (curMonth.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }.get(Calendar.DAY_OF_WEEK) - 1

        // 이번 달 총 일수
        val maxDayThisMonth = curMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 이전 달 계산
        val prevMonth = (curMonth.clone() as Calendar).apply {
            add(Calendar.MONTH, -1)
        }
        val maxDayPrevMonth = prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)

        // 총 6행 * 7열 = 42칸 기준
        val totalCells = 42
        var dayCounter = 1
        var nextMonthDayCounter = 1

        // 이전 달에서 몇 일 보여줄지
        val prevStartDay = maxDayPrevMonth - firstDayOfWeek + 1

        for (i in 0 until totalCells) {
            val dayView = TextView(requireContext()).apply {
                textSize = 15f
                gravity = Gravity.CENTER
                setPadding(0, 24, 0, 10)
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }

                val cellCal = calendar.clone() as Calendar

                when {
                    // 이전 달
                    i < firstDayOfWeek -> {
                        val day = prevStartDay + i
                        text = day.toString()
                        cellCal.add(Calendar.MONTH, -1)
                        cellCal.set(Calendar.DAY_OF_MONTH, day)
                        setTextColorByDate(cellCal)
                        isEnabled = false
                    }

                    // 이번 달
                    i < firstDayOfWeek + maxDayThisMonth -> {
                        val day = dayCounter++
                        text = day.toString()
                        cellCal.set(Calendar.DAY_OF_MONTH, day)
                        setTextColorByDate(cellCal)
                        if (!cellCal.before(today)) {
                            setOnClickListener {
                                onDateSelected(cellCal)
                                dismiss()
                            }
                        }
                    }

                    // 다음 달
                    else -> {
                        val day = nextMonthDayCounter++
                        text = day.toString()
                        cellCal.add(Calendar.MONTH, 1)
                        cellCal.set(Calendar.DAY_OF_MONTH, day)
                        setTextColorByDate(cellCal)
                        isEnabled = false
                    }
                }
            }
            grid.addView(dayView)
        }
    }

    /** 날짜별 색상 처리 */
    private fun TextView.setTextColorByDate(cellCal: Calendar) {
        when {
            cellCal.before(today) -> setTextColor("#9AA1AF".toColorInt()) // 지난 날짜
            cellCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> setTextColor("#FE6B6B".toColorInt()) // 일요일
            else -> setTextColor("#222222".toColorInt()) // 일반
        }
    }
}
