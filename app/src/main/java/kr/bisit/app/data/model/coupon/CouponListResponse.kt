package kr.bisit.app.data.model.coupon

data class CouponListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: CouponListData
)

data class CouponListData(
    val coupons: CouponPage
)

data class CouponPage(
    val content: List<Coupon>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

data class Coupon(
    val couponIssueId: Long,
    val name: String,
    val description: String,
    val amount: Int,
    val percent: Int,
    val validTo: String,
    val isUsed: Boolean
)
