package com.example.bisit.ui.myPageOwner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentOwnerCouponManageBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.example.bisit.R
import com.example.bisit.databinding.SheetOwnerCouponMoreBinding

class OwnerCouponManageFragment : Fragment() {

    private var _binding: FragmentOwnerCouponManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OwnerCouponAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOwnerCouponManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        loadMockData()
    }

    private fun setupRecyclerView() {
        adapter = OwnerCouponAdapter { coupon ->
            showMoreBottomSheet(coupon)
        }
        binding.rvCoupons.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCoupons.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnAddCoupon.setOnClickListener {
            showAddCouponDialog()
        }
    }

    private fun loadMockData() {
        val mockCoupons = listOf(
            OwnerCoupon("1", "20%", "[첫 구매 적용 쿠폰]", "첫 구매 20% 할인 쿠폰입니다. 이 너비까지의 텍스트가 출력되고 이후로는 다음 줄로 내려갑니다. 이후로는 쿠폰 상세 설명입니다.", 1, "2025년 9월 22일"),
            OwnerCoupon("2", "3,000원", "[재예약 대상 고객] 할인 쿠폰", "첫 구매 20% 할인 쿠폰입니다. 이 너비까지의 텍스트가 출력되고 이후로는 다음 줄로 내려갑니다. 이후로는 쿠폰 상세 설명입니다.", 4, "2025년 9월 25일")
        )
        adapter.submitList(mockCoupons)
    }

    private fun showAddCouponDialog() {
        val dialog = DialogAddCoupon(requireContext()) { newCoupon ->
            // In a real app, we'd call the API here.
        }
        dialog.show()
    }

    private fun showMoreBottomSheet(coupon: OwnerCoupon) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val bindingSheet = SheetOwnerCouponMoreBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.btnDelete.setOnClickListener {
            // Handle delete
            bottomSheet.dismiss()
        }

        bindingSheet.btnEdit.setOnClickListener {
            // Handle edit (show dialog with existing data)
            bottomSheet.dismiss()
            showEditCouponDialog(coupon)
        }

        bottomSheet.show()
    }

    private fun showEditCouponDialog(coupon: OwnerCoupon) {
        // Similar to add dialog but with pre-filled data
        val dialog = DialogAddCoupon(requireContext(), coupon) { updatedCoupon ->
            // API call
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
