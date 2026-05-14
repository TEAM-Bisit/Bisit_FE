package kr.bisit.app.ui.myPage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.bisit.app.databinding.ItemMypageCouponBinding

import kr.bisit.app.data.model.coupon.Coupon
import java.text.SimpleDateFormat
import java.util.Locale

class MyPageCouponAdapter(private val coupons: List<Coupon>) :
    RecyclerView.Adapter<MyPageCouponAdapter.CouponViewHolder>() {

    inner class CouponViewHolder(private val binding: ItemMypageCouponBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Coupon) {
            // Display percent or amount
            if (item.percent > 0) {
                 binding.tvCouponTitlePercent.text = "${item.percent}%"
            } else {
                 binding.tvCouponTitlePercent.text = "${item.amount}원"
            }
            
            binding.tvCouponTitle.text = item.name
            binding.tvCouponDesc.text = item.description
            
            // Format Valid Date
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일까지 사용 가능", Locale.KOREA)
            try {
                val date = inputFormat.parse(item.validTo)
                binding.tvCouponMeta.text = if (date != null) outputFormat.format(date) else item.validTo
                
                // Calculate D-Day
                if (date != null) {
                    val diff = date.time - System.currentTimeMillis()
                    val days = diff / (1000 * 60 * 60 * 24)
                    binding.tvCouponMetaDday.text = if (days >= 0) "${days}일 남음" else "만료됨"
                }

            } catch (e: Exception) {
                binding.tvCouponMeta.text = item.validTo
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val binding = ItemMypageCouponBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CouponViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(coupons[position])
    }

    override fun getItemCount(): Int = coupons.size
}
