package kr.bisit.app.data.model.payment

data class PaymentFailRequest(
    val orderId: String,
    val failReason: String
)
