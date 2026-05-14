package kr.bisit.app.data.model.payment

enum class PaymentMethod {
    CARD,               // 카드
    VIRTUAL_ACCOUNT,    // 가상계좌
    TRANSFER,           // 계좌이체
    MOBILE,             // 휴대폰
    EASY_PAY,           // 간편결제 (토스페이, 카카오페이, 네이버페이, 페이코 등)
    GIFT_CERTIFICATE    // 상품권
}
