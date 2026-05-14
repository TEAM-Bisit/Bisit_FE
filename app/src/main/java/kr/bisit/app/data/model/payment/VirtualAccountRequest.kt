package kr.bisit.app.data.model.payment

data class VirtualAccountRequest(
    val orderId: String,
    val amount: Long,
    val bank: String,
    val customerName: String
)
