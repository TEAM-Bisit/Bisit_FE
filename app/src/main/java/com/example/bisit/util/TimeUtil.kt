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
                isoString + "Z" // Offset이 없으면 UTC로 가정하거나 로컬로 처리해야 함. 일단 Z 추가하여 parse 가능하게 함
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
}
