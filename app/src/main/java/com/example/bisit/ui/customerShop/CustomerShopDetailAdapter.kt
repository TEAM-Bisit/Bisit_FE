package com.example.bisit.ui.customerShop

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.shop.ServiceItem
import com.example.bisit.data.model.shop.ReviewItem
import com.example.bisit.data.model.shop.ShopDetailItem
import com.example.bisit.databinding.DialogCopyAddressBinding
import com.example.bisit.databinding.ItemShopDetailBinding

class CustomerShopDetailAdapter(
    private val items: List<ShopDetailItem>,
    private val servicesLists: List<List<ServiceItem>> = emptyList(),
    private val reviewsLists: List<List<ReviewItem>> = emptyList()
) : RecyclerView.Adapter<CustomerShopDetailAdapter.ShopDetailViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    inner class ShopDetailViewHolder(val binding: ItemShopDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopDetailItem, pos: Int) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.tvReview.text = item.review
            binding.tvRating.text = item.rating
            binding.tvSummary.text = item.summary
            binding.tvAddress.text = item.address
            binding.tvOpenInfo.text = item.openInfo
            binding.tvPhone.text = item.phone
            binding.tvNoticeText.text = item.notice
            binding.tvNoticeTime.text = item.noticeTime

            val marginInPx = (2 * binding.root.context.resources.displayMetrics.density).toInt()
            binding.layoutOpenHourDetail.removeAllViews()
            item.weeklyOpenHours.forEach { hour ->
                val tv = TextView(binding.root.context).apply {
                    text = hour
                    textSize = 13f
                    setTextColor(0xFF222222.toInt())
                    includeFontPadding = false
                }
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, marginInPx, 0, marginInPx)
                }
                tv.layoutParams = params
                binding.layoutOpenHourDetail.addView(tv)
            }

            val isExpanded = expandedPositions.contains(pos)
            binding.layoutOpenHourDetail.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.btnExpandHour.rotation = if (isExpanded) 180f else 0f
            binding.btnExpandHour.setOnClickListener {
                if (expandedPositions.contains(pos)) expandedPositions.remove(pos)
                else expandedPositions.add(pos)
                notifyItemChanged(pos)
            }

            binding.layoutAddress.setOnClickListener {
                showCopyAddressDialog(binding.root.context, item.address)
            }

            val inflater = LayoutInflater.from(binding.root.context)
            binding.containerServiceItems.removeAllViews()
            binding.containerReviewItems.removeAllViews()

            val servicesForThis = servicesLists.getOrNull(pos) ?: emptyList()
            servicesForThis.forEach { svc ->
                val view = inflater.inflate(R.layout.item_shop_service, binding.containerServiceItems, false)
                view.findViewById<TextView>(R.id.tvServiceName)?.text = svc.name
                view.findViewById<TextView>(R.id.tvServiceDesc)?.text = svc.desc ?: ""
                view.findViewById<TextView>(R.id.tvServicePrice)?.text = svc.price ?: ""
                view.findViewById<TextView>(R.id.tvServiceTime)?.text = svc.time ?: ""
                binding.containerServiceItems.addView(view)
            }

            val reviewsForThis = reviewsLists.getOrNull(pos) ?: emptyList()
            reviewsForThis.forEach { rev ->
                val view = inflater.inflate(R.layout.item_shop_review, binding.containerReviewItems, false)
                view.findViewById<TextView>(R.id.tvReviewContent)?.text = rev.content
                view.findViewById<TextView>(R.id.tvReviewDate)?.text = rev.date
                view.findViewById<TextView>(R.id.tvReviewer)?.text = rev.author
                binding.containerReviewItems.addView(view)
            }

            binding.tabService.setOnClickListener {
                binding.containerServiceItems.visibility = View.VISIBLE
                binding.containerReviewItems.visibility = View.GONE
                binding.tabService.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tabReview.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                binding.tabService.setBackgroundResource(R.drawable.bg_tab_selected)
                binding.tabReview.setBackgroundResource(R.drawable.bg_tab_unselected)
            }

            binding.tabReview.setOnClickListener {
                binding.containerServiceItems.visibility = View.GONE
                binding.containerReviewItems.visibility = View.VISIBLE
                binding.tabService.setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                binding.tabReview.setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                binding.tabService.setBackgroundResource(R.drawable.bg_tab_unselected)
                binding.tabReview.setBackgroundResource(R.drawable.bg_tab_selected)
            }

            binding.containerServiceItems.visibility = View.VISIBLE
            binding.containerReviewItems.visibility = View.GONE
            binding.tabService.apply {
                setTextColor(ContextCompat.getColor(binding.root.context, R.color.black))
                setBackgroundResource(R.drawable.bg_tab_selected)
            }
            binding.tabReview.apply {
                setTextColor(ContextCompat.getColor(binding.root.context, R.color.gray))
                setBackgroundResource(R.drawable.bg_tab_unselected)
            }
        }

        private fun showCopyAddressDialog(context: Context, address: String) {
            val dialog = Dialog(context)
            val dialogBinding = DialogCopyAddressBinding.inflate(LayoutInflater.from(context))
            dialog.setContentView(dialogBinding.root)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(true)

            dialogBinding.tvAddress.text = address

            dialogBinding.btnCopy.setOnClickListener {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("주소", address))
                Toast.makeText(context, "주소가 복사되었습니다.", Toast.LENGTH_SHORT).show()
            }

            dialogBinding.btnClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopDetailViewHolder {
        val binding = ItemShopDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopDetailViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
