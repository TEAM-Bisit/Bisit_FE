package kr.bisit.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.R
import kr.bisit.app.data.model.shop.ReviewManageItem
import kr.bisit.app.databinding.ItemReviewBinding
import kr.bisit.app.util.DateFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val onMoreClick: (ReviewManageItem) -> Unit
) : ListAdapter<ReviewManageItem, ReviewAdapter.VH>(DIFF) {

    companion object {

        private val DIFF = object : DiffUtil.ItemCallback<ReviewManageItem>() {
            override fun areItemsTheSame(
                oldItem: ReviewManageItem,
                newItem: ReviewManageItem
            ): Boolean {
                return oldItem.reviewId == newItem.reviewId
            }

            override fun areContentsTheSame(
                oldItem: ReviewManageItem,
                newItem: ReviewManageItem
            ): Boolean = oldItem == newItem
        }

        private val apiDateParser =
            SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
    }

    inner class VH(val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            ItemReviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)

        holder.binding.apply {

            // API 24 대응 날짜 처리
            val parsedDate = apiDateParser.parse(item.visitDate)
            val formattedDate = parsedDate?.let {
                DateFormatter.formatReviewVisitDate(it)
            } ?: ""

            tvVisitDate.text =
                root.context.getString(
                    R.string.review_visit_date,
                    formattedDate
                )

            tvServiceName.text = item.serviceName
            tvCustomerName.text = item.reviewerName
            tvReviewContent.text = item.content

            val stars = listOf(star1, star2, star3, star4, star5)
            stars.forEachIndexed { index, imageView ->
                imageView.setImageResource(
                    if (index < item.rating)
                        R.drawable.ic_star_filled
                    else
                        R.drawable.ic_star_empty
                )
            }

            btnMore.setOnClickListener {
                onMoreClick(item)
            }
        }
    }
}
