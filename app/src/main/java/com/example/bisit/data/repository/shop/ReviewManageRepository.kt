package com.example.bisit.data.repository.shop

import android.content.Context
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.ReviewPage

class ReviewManageRepository(
    context: Context
) {

    private val reviewManageApi =
        RetrofitClient.getReviewManageApi(context)

    /** 매장 관리자 리뷰 목록 조회 */
    suspend fun fetchShopReviews(
        shopId: Long,
        page: Int = 0,
        size: Int = 10
    ): ReviewPage {
        return reviewManageApi
            .getShopReviews(
                shopId = shopId,
                page = page,
                size = size
            )
            .data
            .reviews
    }

    /** 매장 관리자 리뷰 삭제 */
    suspend fun deleteReview(
        shopId: Long,
        reviewId: Long
    ) {
        reviewManageApi.deleteReview(
            reviewId = reviewId,
            shopId = shopId
        )
    }
}
