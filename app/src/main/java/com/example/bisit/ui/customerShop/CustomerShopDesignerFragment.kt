package com.example.bisit.ui.customerShop

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.customerShop.StaffData
import kotlinx.coroutines.launch

class CustomerShopDesignerFragment : Fragment() {

    private var selectedPosition = -1
    private var shopId: Long = -1L

    private lateinit var recyclerDesigner: RecyclerView
    private lateinit var btnBook: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView

    private lateinit var designerAdapter: CustomerShopDesignerAdapter
    private var staffList: List<StaffData> = emptyList()
    private var shopName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_designer, container, false)

        recyclerDesigner = view.findViewById(R.id.recyclerDesigner)
        btnBook = view.findViewById(R.id.btnBook)
        btnBack = view.findViewById(R.id.btnBack)
        btnHome = view.findViewById(R.id.btnHome)
        
        // arguments에서 shopId 가져오기
        shopId = arguments?.getLong("shopId") ?: 3L
        shopName = arguments?.getString("shopName") ?: ""

        recyclerDesigner.layoutManager = LinearLayoutManager(requireContext())

        designerAdapter = CustomerShopDesignerAdapter(staffList) { pos ->
            selectedPosition = if (selectedPosition != pos) pos else -1
            designerAdapter.selectedPosition = selectedPosition
            designerAdapter.notifyDataSetChanged()
            updateBookButtonState()
        }

        recyclerDesigner.adapter = designerAdapter
        updateBookButtonState()

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_shopDesignerFragment_to_customerShopFragment)
        }

        btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_shopDesignerFragment_to_homeListFragment)
        }

        btnBook.setOnClickListener {
            if (selectedPosition >= 0) {
                val selectedStaff = staffList[selectedPosition]
                val bundle = Bundle().apply {
                    putLong("staffId", selectedStaff.staffId)
                    putLong("shopId", shopId)
                    putString("staffName", selectedStaff.staffName)
                    putString("staffImage", selectedStaff.image)
                    putInt("reviewCount", selectedStaff.reviewCount)
                    putString("shopName", shopName)
                }
                findNavController().navigate(R.id.customerReserveFragment, bundle)
            }
        }


        // API 호출
        loadStaffList()

        return view
    }

    private fun loadStaffList() {
        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getCustomerShopApi(requireContext())
                val response = api.getStaffList(shopId)
                
                if (response.isSuccessful && response.body() != null) {
                    val staffResponse = response.body()!!
                    if (staffResponse.success && staffResponse.data.isNotEmpty()) {
                        staffList = staffResponse.data
                        designerAdapter.updateData(staffList)
                        Log.d("CustomerShopDesignerFragment", "Staff list loaded: ${staffList.size} items")
                    } else {
                        Log.w("CustomerShopDesignerFragment", "Empty staff list received")
                        Toast.makeText(requireContext(), "승인된 직원이 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("CustomerShopDesignerFragment", "API call failed: ${response.code()}")
                    Toast.makeText(requireContext(), "직원 목록을 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("CustomerShopDesignerFragment", "Error loading staff list", e)
                Toast.makeText(requireContext(), "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateBookButtonState() {
        val enabled = selectedPosition >= 0
        btnBook.isEnabled = enabled
        if (enabled) {
            btnBook.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4076FF"))
            btnBook.setTextColor(Color.WHITE)
        } else {
            btnBook.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btnBook.setTextColor(Color.parseColor("#9E9E9E"))
        }
    }
}
