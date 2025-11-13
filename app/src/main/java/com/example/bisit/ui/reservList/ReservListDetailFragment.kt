package com.example.bisit.ui.reservList

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.bisit.R
import com.example.bisit.databinding.FragmentReservListDetailBinding
import com.example.bisit.ui.reservList.model.ReservListItem

class ReservListDetailFragment : Fragment(R.layout.fragment_reserv_list_detail) {

    private var _binding: FragmentReservListDetailBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReservListDetailBinding.bind(view)

        // ReservListFragment에서 전달된 데이터 받기
        val data = arguments?.getSerializable("reservItem") as? ReservListItem ?: return

        // 데이터 표시
        binding.tvService.text = data.serviceName
        binding.tvDate.text = data.dateTime
        binding.tvName.text = data.customerName
        binding.tvPhone.text = data.phone
        binding.tvAddress.text = data.address
        binding.tvPrice.text = "${data.price}원"

        /** ✅ 예약 상태(status)에 따라 입금 상태 버튼 색상 변경 **/
        when (data.status) {
            "예약 확정" -> {
                // 입금 완료 (진회색)
                binding.btnPaymentStatus.text = "입금 완료"
                binding.btnPaymentStatus.setTextColor(Color.WHITE)
                binding.btnPaymentStatus.setBackgroundColor("#515965".toColorInt())
            }

            "예약 확인 중" -> {
                // 입금 완료 (밝은 회색)
                binding.btnPaymentStatus.text = "입금 완료"
                binding.btnPaymentStatus.setTextColor("#515965".toColorInt())
                binding.btnPaymentStatus.setBackgroundColor("#F0F2F5".toColorInt())
            }

            else -> {
                // 예외처리 (혹시 다른 상태가 생겼을 때)
                binding.btnPaymentStatus.text = data.status
                binding.btnPaymentStatus.setTextColor(Color.DKGRAY)
                binding.btnPaymentStatus.setBackgroundColor(Color.LTGRAY)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
