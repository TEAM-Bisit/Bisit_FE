package kr.bisit.app.ui.shop.model

import kr.bisit.app.data.model.shop.TreatmentRequest
import kr.bisit.app.data.model.shop.TreatmentResponse

fun TreatmentResponse.toRequest(): TreatmentRequest {
    return TreatmentRequest(
        name = name,
        description = description,
        price = price,
        durationHours = durationHours,
        durationMinutes = durationMinutes,
        isActive = isActive
    )
}
