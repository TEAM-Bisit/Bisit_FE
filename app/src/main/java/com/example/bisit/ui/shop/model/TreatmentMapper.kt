package com.example.bisit.ui.shop.model

import com.example.bisit.data.model.shop.TreatmentRequest
import com.example.bisit.data.model.shop.TreatmentResponse

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
