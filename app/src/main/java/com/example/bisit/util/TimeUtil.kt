package com.example.bisit.utils

import android.util.Log
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException
import java.util.*

object TimeUtil {
    fun toRelativeTimeKorean(isoString: String?): String {
        if (isoString.isNullOrBlank()) {
            return ""
        }
        return try {
            // "2025-12-29T12:41:51.864Z" 또는 "2025-12-29T12:41:51" 등 다양한 형식 대응
            val normalized = if (isoString.contains("T") && !isoString.contains("Z") && !isoString.contains("+")) {
                isoString + "+09:00" // Offset이 없으면 KST(+09:00)로 가정
            } else {
                isoString
            }

            val then = OffsetDateTime.parse(normalized)
            val now = OffsetDateTime.now(then.offset)
            val seconds = Duration.between(then, now).seconds

            when {
                seconds < 0 -> "방금" // 미래 시간인 경우 (서버-클라이언트 오차)
                seconds < 60 -> "방금"
                seconds < 3600 -> "${seconds / 60}분 전"
                seconds < 86400 -> "${seconds / 3600}시간 전"
                seconds < 2592000 -> "${seconds / 86400}일 전"
                seconds < 31536000 -> "${seconds / 2592000}개월 전"
                else -> "${seconds / 31536000}년 전"
            }
        } catch (e: Exception) {
            Log.e("TimeUtil", "Failed to parse date: $isoString", e)
            // 파싱 실패 시 "YYYY-MM-DD" 부분만이라도 반환 (이상한 시간/밀리초 등 제거)
            if (isoString.length >= 10) {
                isoString.substring(0, 10).replace("T", " ")
            } else {
                isoString
            }
        }
    }

    fun checkOpenStatus(businessHours: String?): String {
        if (businessHours.isNullOrBlank()) return "정보 없음"
        if (businessHours.contains("휴무")) return "오늘 휴무"
        if (businessHours.contains("24시간")) return "영업 중"

        return try {
            // Expected "HH:mm ~ HH:mm"
            val parts = businessHours.split("~").map { it.trim() }
            if (parts.size != 2) return "" 

            val now = java.time.LocalTime.now(java.time.ZoneId.of("Asia/Seoul"))
            val start = java.time.LocalTime.parse(parts[0])
            val end = java.time.LocalTime.parse(parts[1])

            if (start.isBefore(end)) {
                if (now.isAfter(start) && now.isBefore(end)) "영업 중" else "영업 종료"
            } else {
                // Cross midnight
                if (now.isAfter(start) || now.isBefore(end)) "영업 중" else "영업 종료"
            }
        } catch (e: Exception) {
            ""
        }
    }
}
