package com.example.bisit.utils

import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.*

object TimeUtil {
    fun toRelativeTimeKorean(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            // 서버가 "2025-11-25T04:15:18.396Z" 같은 Z 포함 ISO를 주면 OffsetDateTime.parse 가능
            val then = OffsetDateTime.parse(isoString)
            val now = OffsetDateTime.now(then.offset) // 동일 오프셋으로 비교
            val dur = Duration.between(then, now)

            val seconds = dur.seconds
            when {
                seconds < 60 -> "방금"
                seconds < 60 * 60 -> "${seconds / 60}분 전"
                seconds < 60 * 60 * 24 -> "${seconds / (60 * 60)}시간 전"
                seconds < 60 * 60 * 24 * 30 -> "${seconds / (60 * 60 * 24)}일 전"
                seconds < 60L * 60 * 24 * 365 -> "${seconds / (60L * 60 * 24 * 30)}개월 전"
                else -> "${seconds / (60L * 60 * 24 * 365)}년 전"
            }
        } catch (e: DateTimeParseException) {
            isoString
        }
    }
}
