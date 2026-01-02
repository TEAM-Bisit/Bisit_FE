package com.example.bisit.data.model.shop

/** 공통 API 응답 */
data class ApiResponse<T>(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: T
)

/** 공지사항 단건 */
data class ShopNoticeResponse(
    val id: Long,
    val title: String,
    val content: String,
    val createdAt: String
)

/** 공지사항 목록 */
data class ShopNoticeListResponse(
    val notices: List<ShopNoticeResponse>
)

/** 생성 / 수정 요청 */
data class ShopNoticeRequest(
    val title: String,
    val content: String
)
