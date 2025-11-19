package com.example.bisit.ui.customerPay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.R
import com.example.bisit.databinding.FragmentCustomerPayBinding

class CustomerPayFragment : Fragment() {

    private var _binding: FragmentCustomerPayBinding? = null
    private val binding get() = _binding!!

    private val addressLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val addr = data?.getStringExtra("selectedAddress") ?: ""
            val road = data?.getStringExtra("roadAddress") ?: ""
            val jibun = data?.getStringExtra("jibunAddress") ?: ""

            binding.tvSelectedAddress.text =
                if (addr.isNotEmpty()) addr else (road.ifEmpty { jibun })
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerPayBinding.inflate(inflater, container, false)

        setupCouponClick()
        setupCheckBox()
        setupExpandableLayouts()
        setupPayButton()
        setupAddressSearch()

        return binding.root
    }

    private fun setupCouponClick() {
        binding.layoutCoupon.setOnClickListener {
            findNavController().navigate(
                R.id.action_customerPayFragment_to_customerPayCouponFragment
            )
        }
    }

    private fun setupCheckBox() {
        binding.cbAgree.setOnCheckedChangeListener { _, isChecked ->
            binding.btnPay.apply {
                isEnabled = isChecked
                backgroundTintList = resources.getColorStateList(
                    if (isChecked) R.color.blue_4076FF else R.color.gray,
                    null
                )
            }
        }
    }

    private fun setupExpandableLayouts() {
        setupExpandable(
            header = binding.headerSellerInfo,
            content = binding.contentSellerInfo,
            arrow = binding.sellerArrow
        )

        setupExpandable(
            header = binding.headerPrivacyInfo,
            content = binding.contentPrivacyInfo,
            arrow = binding.privacyArrow
        )

        setupExpandable(
            header = binding.headerCancelRule,
            content = binding.contentCancelRule,
            arrow = binding.cancelArrow
        )
    }

    private fun setupExpandable(header: View, content: View, arrow: ImageView) {
        content.visibility = View.GONE
        arrow.rotation = 0f

        header.setOnClickListener {
            val expand = content.visibility == View.GONE

            if (expand) {
                content.visibility = View.VISIBLE
                arrow.animate().rotation(180f).setDuration(200).start()
            } else {
                content.visibility = View.GONE
                arrow.animate().rotation(0f).setDuration(200).start()
            }
        }
    }

    private fun setupPayButton() {
        binding.btnPay.setOnClickListener {
            startActivity(Intent(requireContext(), TossPayActivity::class.java))
        }
    }

    private fun setupAddressSearch() {
        binding.btnSearchAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddressSearchActivity::class.java)
            addressLauncher.launch(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
