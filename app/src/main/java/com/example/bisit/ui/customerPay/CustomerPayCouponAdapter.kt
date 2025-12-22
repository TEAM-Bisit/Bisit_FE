package com.example.bisit.ui.customerPay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.coupon.ApplicableCoupon
import java.text.SimpleDateFormat
import java.util.Locale

class CustomerPayCouponAdapter(
    private val coupons: List<ApplicableCoupon>,
    private val onCouponClick: (ApplicableCoupon) -> Unit
) : RecyclerView.Adapter<CustomerPayCouponAdapter.CouponViewHolder>() {

    private var selectedPosition = -1

    inner class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radio: RadioButton = itemView.findViewById(R.id.radioCouponSelect)
        val percent: TextView = itemView.findViewById(R.id.tv_coupon_title_percent)
        val title: TextView = itemView.findViewById(R.id.tv_coupon_title)
        val desc: TextView = itemView.findViewById(R.id.tv_coupon_desc)
        val dday: TextView = itemView.findViewById(R.id.tv_coupon_meta_dday)
        val date: TextView = itemView.findViewById(R.id.tv_coupon_meta)

        fun bind(item: ApplicableCoupon, position: Int) {
            
            if (item.type == "PERCENT") {
               percent.text = "${item.percent}%"
            } else {
               percent.text = "${item.amount}원"
            }
            
            title.text = item.name
            desc.text = item.description
            
             // Format Date and D-Day
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일까지 사용 가능", Locale.KOREA)
            try {
                val parsedDate = inputFormat.parse(item.validTo)
                date.text = if (parsedDate != null) outputFormat.format(parsedDate) else item.validTo
                 if (parsedDate != null) {
                    val diff = parsedDate.time - System.currentTimeMillis()
                    val days = diff / (1000 * 60 * 60 * 24)
                   dday.text = if (days >= 0) "${days}일 남음" else "만료됨"
                }

            } catch (e: Exception) {
                date.text = item.validTo
            }

            // 단일 선택 처리
            radio.isChecked = (selectedPosition == position)

            itemView.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                onCouponClick(item)
            }

            radio.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
                onCouponClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pay_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(coupons[position], position)
    }

    override fun getItemCount(): Int = coupons.size
}
