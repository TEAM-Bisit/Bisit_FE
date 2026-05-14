package kr.bisit.app.data.repository.coupon

import kr.bisit.app.data.api.CouponApiService
import kr.bisit.app.data.model.auth.AuthResponse
import kr.bisit.app.data.model.coupon.CreateCouponRequest
import kr.bisit.app.data.model.coupon.OwnerCouponResponse
import kr.bisit.app.data.model.coupon.SingleCouponResponse
import kr.bisit.app.data.model.coupon.UpdateCouponRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

class OwnerCouponRepository(private val apiService: CouponApiService) {

    suspend fun getShopCoupons(shopId: Long, page: Int = 0, size: Int = 10): OwnerCouponResponse = suspendCancellableCoroutine { continuation ->
        apiService.getShopCoupons(shopId, page, size).enqueue(object : Callback<OwnerCouponResponse> {
            override fun onResponse(call: Call<OwnerCouponResponse>, response: Response<OwnerCouponResponse>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body()!!)
                } else {
                    continuation.resumeWithException(Exception("Failed to fetch coupons: ${response.code()}"))
                }
            }
            override fun onFailure(call: Call<OwnerCouponResponse>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

    suspend fun createCoupon(shopId: Long, request: CreateCouponRequest): SingleCouponResponse = suspendCancellableCoroutine { continuation ->
        apiService.createShopCoupon(shopId, request).enqueue(object : Callback<SingleCouponResponse> {
            override fun onResponse(call: Call<SingleCouponResponse>, response: Response<SingleCouponResponse>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body()!!)
                } else {
                    continuation.resumeWithException(Exception("Failed to create coupon: ${response.code()}"))
                }
            }
            override fun onFailure(call: Call<SingleCouponResponse>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

    suspend fun updateCoupon(couponId: Long, shopId: Long, request: UpdateCouponRequest): SingleCouponResponse = suspendCancellableCoroutine { continuation ->
        apiService.updateShopCoupon(couponId, shopId, request).enqueue(object : Callback<SingleCouponResponse> {
            override fun onResponse(call: Call<SingleCouponResponse>, response: Response<SingleCouponResponse>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body()!!)
                } else {
                    continuation.resumeWithException(Exception("Failed to update coupon: ${response.code()}"))
                }
            }
            override fun onFailure(call: Call<SingleCouponResponse>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }

    suspend fun deleteCoupon(couponId: Long, shopId: Long): AuthResponse = suspendCancellableCoroutine { continuation ->
        apiService.deleteShopCoupon(couponId, shopId).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful) {
                    continuation.resume(response.body()!!)
                } else {
                    continuation.resumeWithException(Exception("Failed to delete coupon: ${response.code()}"))
                }
            }
            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                continuation.resumeWithException(t)
            }
        })
    }
}
