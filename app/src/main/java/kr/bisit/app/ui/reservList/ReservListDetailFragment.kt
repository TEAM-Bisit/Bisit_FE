package kr.bisit.app.ui.reservList

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import kr.bisit.app.R
import kr.bisit.app.databinding.FragmentReservListDetailBinding

class ReservListDetailFragment : Fragment(R.layout.fragment_reserv_list_detail) {

    private var _binding: FragmentReservListDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReservListDetailViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReservListDetailBinding.bind(view)

        val reservationId = arguments?.getLong("reservationId") ?: return

        observeViewModel()
        viewModel.loadReservationDetail(reservationId)
    }

    private fun observeViewModel() {
        viewModel.reservationDetail.observe(viewLifecycleOwner) { data ->

            // ===== 기존 UI 그대로 =====
            binding.tvService.text = data.treatmentName
            binding.tvDate.text = "${data.reservedDate} ${data.startTime}"
            binding.tvName.text = data.customerName
            binding.tvPhone.text = data.customerPhone
            binding.tvAddress.text = data.customerAddress
            binding.tvPrice.text = "${data.price}원"

            // ===== 입금 버튼 (문구 고정) =====
            binding.btnPaymentStatus.text = "입금 완료"

            when (data.status) {
                "CONFIRMED", "COMPLETED" -> {
                    binding.btnPaymentStatus.setTextColor(Color.WHITE)
                    binding.btnPaymentStatus.setBackgroundColor("#515965".toColorInt())
                }
                else -> {
                    binding.btnPaymentStatus.setTextColor("#515965".toColorInt())
                    binding.btnPaymentStatus.setBackgroundColor("#F0F2F5".toColorInt())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
