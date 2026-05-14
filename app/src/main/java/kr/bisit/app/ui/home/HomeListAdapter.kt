package kr.bisit.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.data.model.category.CategoryShopItem
import kr.bisit.app.databinding.ItemHomeStoreBinding

class HomeListAdapter(
    private var items: List<CategoryShopItem>,
    private val onClick: (CategoryShopItem) -> Unit
) : RecyclerView.Adapter<HomeListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemHomeStoreBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHomeStoreBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        binding.imgRecycler.layoutManager =
            LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvName.text = item.shopName
        holder.binding.tvCategory.text = item.category
        val formattedRating = String.format("%.1f", item.averageRating)
        holder.binding.tvRating.text = formattedRating
        holder.binding.tvReviewCount.text = "(${item.reviewCount})"
        holder.binding.tvBusinessHours.text = item.businessHours ?: ""

        // Open Status Logic
        val status = kr.bisit.app.utils.TimeUtil.checkOpenStatus(item.businessHours)
        if (status == "영업 중") {
            holder.binding.tvOpenStatus.text = "영업 중"
            holder.binding.tvOpenStatus.setTextColor(android.graphics.Color.parseColor("#333333")) // Dark Gray/Black
            holder.binding.imgOpenStatus.visibility = android.view.View.VISIBLE
        } else if (status == "오늘 휴무") {
            holder.binding.tvOpenStatus.text = "오늘 휴무"
            holder.binding.tvOpenStatus.setTextColor(android.graphics.Color.parseColor("#E53935")) // Red
            holder.binding.imgOpenStatus.visibility = android.view.View.GONE
        } else if (status == "영업 종료") {
            holder.binding.tvOpenStatus.text = "영업 종료"
            holder.binding.tvOpenStatus.setTextColor(android.graphics.Color.parseColor("#999999")) // Gray
            holder.binding.imgOpenStatus.visibility = android.view.View.GONE
        } else {
            holder.binding.tvOpenStatus.text = status.ifEmpty { "정보 없음" }
            holder.binding.tvOpenStatus.setTextColor(android.graphics.Color.parseColor("#999999"))
            holder.binding.imgOpenStatus.visibility = android.view.View.GONE
        }

        // 별점 표시 로직 (1개 이미지 + 숫자)

        val imageAdapter =
            HomeListImageAdapter(item.photos, item.hasVisitService ?: false)

        holder.binding.imgRecycler.adapter = imageAdapter

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = items.size

    fun updateData(newList: List<CategoryShopItem>) {
        val oldSize = items.size
        items = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun addData(newList: List<CategoryShopItem>) {
        val oldSize = items.size
        (items as MutableList).addAll(newList)
        notifyItemRangeInserted(oldSize, newList.size)
    }

    fun clearData() {
        val oldSize = items.size
        (items as MutableList).clear()
        notifyItemRangeRemoved(0, oldSize)
    }
}
