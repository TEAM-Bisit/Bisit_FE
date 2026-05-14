package kr.bisit.app.data.model.coupon

data class OwnerCouponResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: OwnerCouponData
)

data class OwnerCouponData(
    val coupons: OwnerCouponPage
)

data class OwnerCouponPage(
    val content: List<OwnerCouponItem>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val size: Int,
    val hasNext: Boolean
)

data class OwnerCouponItem(
    val couponId: Long,
    val scope: String,
    val type: String, // AMOUNT, PERCENT
    val name: String,
    val description: String,
    val amount: Int,
    val percent: Int,
    val minOrderAmount: Int,
    val validFrom: String,
    val validTo: String,
    val usageLimit: Int,
    val isActive: Boolean,
    val createdAt: String
)

data class CreateCouponRequest(
    val scope: String = "SHOP",
    val type: String,
    val name: String,
    val description: String,
    val amount: Int,
    val percent: Int,
    val minOrderAmount: Int,
    val validFrom: String,
    val validTo: String,
    val usageLimit: Int
)

data class UpdateCouponRequest(
    val type: String,
    val name: String,
    val description: String,
    val amount: Int,
    val percent: Int,
    val minOrderAmount: Int,
    val validFrom: String,
    val validTo: String,
    val usageLimit: Int
)

data class SingleCouponResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: OwnerCouponItem
)
