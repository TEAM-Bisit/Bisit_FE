package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bisit.databinding.FragmentShopBasicBinding
import com.example.bisit.ui.shop.dialog.EditHoursDialog
import com.example.bisit.ui.shop.dialog.EditSalesDialog
import com.example.bisit.ui.shop.dialog.EditShopInfoDialog


class ShopBasicFragment : Fragment() {
    private var _binding: FragmentShopBasicBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBasicBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnEditShopInfo.setOnClickListener {
            EditShopInfoDialog(onSaved = {
// 서버 저장 성공 시 UI 갱신 등
            }).show(parentFragmentManager, "edit_shop_info")
        }
        binding.btnEditSales.setOnClickListener {
            EditSalesDialog(onResult = { success ->
// 성공/실패 안내는 다이얼로그 내부에서 처리, 필요시 추가 로직
            }).show(parentFragmentManager, "edit_sales")
        }
        binding.btnEditIntro.setOnClickListener {
// 필요 시 별도 다이얼로그 추가 가능
        }
// 예시: 대표 이미지 변경
        binding.btnChangeHeader.setOnClickListener { /* 갤러리/카메라 */ }


// 영업시간 수정(디자인상 특정 버튼이 없다면, 기본 섹션 길게 눌러 띄우기 등의 트리거)
        binding.root.setOnLongClickListener {
            EditHoursDialog(onSaved = { /* 저장 후 반영 */ }).show(parentFragmentManager, "edit_hours")
            true
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}