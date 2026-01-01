package com.example.bisit.data.repository.shop

import android.content.Context
import com.example.bisit.data.api.ShopServiceApiService
import com.example.bisit.data.model.shop.*
import com.example.bisit.data.api.RetrofitClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson

class ShopServiceRepository(context: Context) {

    private val api: ShopServiceApiService =
        RetrofitClient.getShopServiceApi(context)
    private val gson = Gson()

    private fun toJsonRequestBody(request: TreatmentRequest): RequestBody {
        return gson.toJson(request)
            .toRequestBody("application/json".toMediaType())
    }

    suspend fun createTreatment(
        shopId: Long,
        request: TreatmentRequest,
        photoPart: MultipartBody.Part?
    ) = api.createTreatment(
        shopId = shopId,
        request = toJsonRequestBody(request),
        photo = photoPart
    )

    suspend fun updateTreatment(
        treatmentId: Long,
        shopId: Long,
        request: TreatmentRequest,
        photoPart: MultipartBody.Part?
    ) = api.updateTreatment(
        treatmentId = treatmentId,
        shopId = shopId,
        request = toJsonRequestBody(request),
        photo = photoPart
    )

    suspend fun deleteTreatment(
        treatmentId: Long,
        shopId: Long
    ) = api.deleteTreatment(treatmentId, shopId)

    suspend fun getTreatments(
        shopId: Long,
        page: Int,
        size: Int
    ) = api.getTreatments(shopId, page, size)
}
