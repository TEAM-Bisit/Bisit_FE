package com.example.bisit.ui.customerMyReserve

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.databinding.DialogCustomerMyReserveReviewBinding
import com.example.bisit.databinding.FragmentCustomerMyReserveDetailBinding
import com.example.bisit.databinding.ItemCustomerMyReserveDetailHeaderBinding
import com.example.bisit.databinding.ItemCustomerMyReserveDetailBodyBinding

class CustomerMyReserveDetailFragment : Fragment() {

    private var _binding: FragmentCustomerMyReserveDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var headerBinding: ItemCustomerMyReserveDetailHeaderBinding
    private lateinit var bodyBinding: ItemCustomerMyReserveDetailBodyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerMyReserveDetailBinding.inflate(inflater, container, false)

        headerBinding = ItemCustomerMyReserveDetailHeaderBinding.bind(binding.headerContainer.getChildAt(0))
        bodyBinding = ItemCustomerMyReserveDetailBodyBinding.bind(binding.bodyContainer.getChildAt(0))

        setupHeader()
        setupBody()
        setupButtons()

        return binding.root
    }

    private fun setupHeader() {
        headerBinding.apply {
            tvStatus.text = "시술 완료"
            tvReservNo.text = "예약 번호 shtydlqslek"
            tvShopName.text = "다른헤어"
            tvShopAddr.text = "대구 중구 관덕정길 6-11 1층"
            btnReview.setOnClickListener {
                showReviewDialog()
            }
        }
    }

    private fun setupBody() {
        bodyBinding.apply {
            tvValueDate.text = "2025.08.06 오후 1시"
            tvValueTime.text = "1시간 30분"
            tvValuePrice.text = "60,000원"
            tvValueName.text = "라마바"
        }
    }

    private fun setupButtons() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnNextStep.setOnClickListener {
        }
    }

    private fun showReviewDialog() {
        val dialog = Dialog(requireContext())
        val dialogBinding = DialogCustomerMyReserveReviewBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.setOnShowListener {
            val window = dialog.window
            val params = window?.attributes
            val density = resources.displayMetrics.density

            params?.width = (324 * density).toInt()
            params?.height = (352 * density).toInt()
            window?.attributes = params
        }

        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }

        dialogBinding.etReview.addTextChangedListener {
            val textLength = it?.length ?: 0
            dialogBinding.tvTextCount.text = "$textLength/30자"
        }

        dialogBinding.btnSummit.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
