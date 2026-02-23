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

import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.bisit.data.model.coupon.CreateCouponRequest
import com.example.bisit.data.model.coupon.OwnerCouponItem
import com.example.bisit.data.model.coupon.UpdateCouponRequest
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OwnerCouponManageFragment : Fragment() {

    private var _binding: FragmentOwnerCouponManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OwnerCouponAdapter
    private val viewModel: OwnerCouponViewModel by viewModels {
        OwnerCouponViewModelFactory(requireContext())
    }

    private val shopId: Long = 1L // TODO: Get actual shopId from session/pref

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentOwnerCouponManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        viewModel.fetchCoupons(shopId)
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.coupons.collectLatest { coupons ->
                adapter.submitList(coupons)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                // Show/hide loading indicator if exists
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                error?.let {
                    // Show error toast
                    viewModel.clearError()
                }
            }
        }
    }

    private fun showAddCouponDialog() {
        val dialog = DialogAddCoupon(requireContext()) { newCouponRequest ->
            if (newCouponRequest is CreateCouponRequest) {
                viewModel.createCoupon(shopId, newCouponRequest)
            }
        }
        dialog.show()
    }

    private fun showMoreBottomSheet(coupon: OwnerCouponItem) {
        val bottomSheet = BottomSheetDialog(requireContext())
        val bindingSheet = SheetOwnerCouponMoreBinding.inflate(layoutInflater)
        bottomSheet.setContentView(bindingSheet.root)

        bindingSheet.btnDelete.setOnClickListener {
            viewModel.deleteCoupon(coupon.couponId, shopId)
            bottomSheet.dismiss()
        }

        bindingSheet.btnEdit.setOnClickListener {
            bottomSheet.dismiss()
            showEditCouponDialog(coupon)
        }

        bottomSheet.show()
    }

    private fun showEditCouponDialog(coupon: OwnerCouponItem) {
        val dialog = DialogAddCoupon(requireContext(), coupon) { updatedCouponRequest ->
            if (updatedCouponRequest is UpdateCouponRequest) {
                viewModel.updateCoupon(coupon.couponId, shopId, updatedCouponRequest)
            }
        }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
