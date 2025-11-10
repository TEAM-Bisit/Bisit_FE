package com.example.bisit.ui.customerShop

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bisit.R
import com.example.bisit.data.model.shop.Designer

class CustomerShopDesignerFragment : Fragment() {

    private var selectedPosition = -1

    private lateinit var recyclerDesigner: RecyclerView
    private lateinit var btnBook: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnHome: ImageView

    private lateinit var designerAdapter: CustomerShopDesignerAdapter

    private val designers = listOf(
        Designer("김우리", "사장님", "방문 서비스, 예약 시술 모두 가능합니다.", "4.8", "리뷰 12개"),
        Designer("엘라 실장", "사장님", "펌 전문 디자이너입니다.", "4.5", "리뷰 10개")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shop_designer, container, false)

        recyclerDesigner = view.findViewById(R.id.recyclerDesigner)
        btnBook = view.findViewById(R.id.btnBook)
        btnBack = view.findViewById(R.id.btnBack)
        btnHome = view.findViewById(R.id.btnHome)

        recyclerDesigner.layoutManager = LinearLayoutManager(requireContext())

        designerAdapter = CustomerShopDesignerAdapter(designers) { pos ->
            selectedPosition = if (selectedPosition != pos) pos else -1
            designerAdapter.selectedPosition = selectedPosition
            designerAdapter.notifyDataSetChanged()
            updateBookButtonState()
        }

        recyclerDesigner.adapter = designerAdapter
        updateBookButtonState()

        btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_shopDesignerFragment_to_shopFragment)
        }

        btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_shopDesignerFragment_to_homeListFragment)
        }

        btnBook.setOnClickListener {
            if (selectedPosition >= 0) {
                findNavController().navigate(R.id.customerReserveFragment)
            }
        }

        return view
    }

    private fun updateBookButtonState() {
        val enabled = selectedPosition >= 0
        btnBook.isEnabled = enabled
        if (enabled) {
            btnBook.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FE6B6B"))
            btnBook.setTextColor(Color.WHITE)
        } else {
            btnBook.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"))
            btnBook.setTextColor(Color.parseColor("#9E9E9E"))
        }
    }
}
