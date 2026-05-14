package kr.bisit.app.data.model.coupon

data class ApplicableCouponResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: ApplicableCouponData
)

data class ApplicableCouponData(
    val coupons: List<ApplicableCoupon>
)

data class ApplicableCoupon(
    val couponIssueId: Long,
    val name: String,
    val description: String,
    val type: String, // "AMOUNT" or "PERCENT"
    val amount: Int,
    val percent: Int,
    val minOrderAmount: Int,
    val validTo: String,
    val expectedDiscount: Int
) : java.io.Serializable // Serializable for passing via Bundle
