package com.example.bisit.ui.customerPay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.customerReserve.PayCoupon

class CustomerPayCouponAdapter(
    private val items: List<PayCoupon>
) : RecyclerView.Adapter<CustomerPayCouponAdapter.CouponViewHolder>() {

    private var selectedPosition = -1

    inner class CouponViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radio: RadioButton = itemView.findViewById(R.id.radioCouponSelect)
        val percent: TextView = itemView.findViewById(R.id.tv_coupon_title_percent)
        val title: TextView = itemView.findViewById(R.id.tv_coupon_title)
        val desc: TextView = itemView.findViewById(R.id.tv_coupon_desc)
        val dday: TextView = itemView.findViewById(R.id.tv_coupon_meta_dday)
        val date: TextView = itemView.findViewById(R.id.tv_coupon_meta)

        fun bind(item: PayCoupon, position: Int) {
            percent.text = item.percent
            title.text = item.title
            desc.text = item.desc
            dday.text = item.dday
            date.text = item.date

            // 단일 선택 처리
            radio.isChecked = (selectedPosition == position)

            itemView.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
            }

            radio.setOnClickListener {
                selectedPosition = position
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CouponViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pay_coupon, parent, false)
        return CouponViewHolder(view)
    }

    override fun onBindViewHolder(holder: CouponViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
