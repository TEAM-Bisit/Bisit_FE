package kr.bisit.app.data.model.payment

data class PaymentConfirmData(
    val paymentId: Long,
    val reservationId: Long,
    val orderId: String,
    val paymentKey: String,
    val paidAmount: Long,
    val status: String,
    val approvedAt: String
)
