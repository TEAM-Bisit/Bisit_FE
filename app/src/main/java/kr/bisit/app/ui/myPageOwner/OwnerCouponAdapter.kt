package kr.bisit.app.ui.myPageOwner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.databinding.ItemOwnerCouponBinding

import kr.bisit.app.data.model.coupon.OwnerCouponItem
import java.text.SimpleDateFormat
import java.util.*

class OwnerCouponAdapter(
    private val onMoreClicked: (OwnerCouponItem) -> Unit
) : ListAdapter<OwnerCouponItem, OwnerCouponAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemOwnerCouponBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: OwnerCouponItem, onMoreClicked: (OwnerCouponItem) -> Unit) {
            val valueText = if (item.type == "AMOUNT") "${item.amount}원" else "${item.percent}%"
            binding.tvCouponValue.text = valueText
            binding.tvCouponName.text = item.name
            binding.tvCouponDescription.text = item.description
            
            val remainingDays = calculateRemainingDays(item.validTo)
            binding.tvRemainingDays.text = "${remainingDays}일 남음"
            
            // Format expiry date (assuming validTo is ISO8601 or similar)
            val formattedExpiry = formatExpiryDate(item.validTo)
            binding.tvExpiryDate.text = "${formattedExpiry}까지 사용 가능"
            
            binding.btnMore.setOnClickListener { onMoreClicked(item) }
        }

        private fun calculateRemainingDays(validTo: String): Long {
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val cleanDateStr = if (validTo.contains(".")) validTo.substringBefore(".") else validTo.substringBefore("Z")
                val expiryDate = inputSdf.parse(cleanDateStr) ?: return 0
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time
                val diff = expiryDate.time - today.time
                diff / (24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                0
            }
        }

        private fun formatExpiryDate(validTo: String): String {
            return try {
                val inputSdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val cleanDateStr = if (validTo.contains(".")) validTo.substringBefore(".") else validTo.substringBefore("Z")
                val outputSdf = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
                val date = inputSdf.parse(cleanDateStr) ?: return validTo
                outputSdf.format(date)
            } catch (e: Exception) {
                validTo.split("T")[0].replace("-", ".")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOwnerCouponBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onMoreClicked)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<OwnerCouponItem>() {
        override fun areItemsTheSame(oldItem: OwnerCouponItem, newItem: OwnerCouponItem) = oldItem.couponId == newItem.couponId
        override fun areContentsTheSame(oldItem: OwnerCouponItem, newItem: OwnerCouponItem) = oldItem == newItem
    }
}
