package com.example.bisit.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val reviewDateFormatter =
        SimpleDateFormat("yyyy.MM.dd, E", Locale.KOREAN)

    fun formatReviewVisitDate(date: Date): String {
        return reviewDateFormatter.format(date)
    }
}
