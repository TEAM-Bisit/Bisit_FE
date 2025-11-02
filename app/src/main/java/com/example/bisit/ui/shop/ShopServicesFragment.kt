package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentShopServicesBinding
import com.example.bisit.ui.shop.adapter.ServiceAdapter
import com.example.bisit.ui.shop.dialog.AddServiceDialog
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import com.example.bisit.ui.shop.model.ServiceItem

class ShopServicesFragment : Fragment() {

    private var _binding: FragmentShopServicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ServiceAdapter

    private val data = mutableListOf(
        ServiceItem(1, "일반 컷트", "가장 기본적인 컷트", 25000),
        ServiceItem(2, "볼륨매직", "두피 볼륨을 살리는 시술", 80000)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopServicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 지역변수 X, 전역 adapter 초기화
        adapter = ServiceAdapter(onMoreClick = { item ->
            BottomActionSheet(
                onDelete = {
                    ConfirmDialog("삭제하시겠어요?", onOk = {
                        data.removeIf { it.id == item.id }
                        adapter.submitList(data.toList())
                    }).show(parentFragmentManager, "confirm")
                },
                onEdit = {
                    AddServiceDialog(prefill = item) { updated ->
                        val idx = data.indexOfFirst { it.id == updated.id }
                        if (idx >= 0) {
                            data[idx] = updated
                            adapter.submitList(data.toList())
                        }
                    }.show(parentFragmentManager, "edit_service")
                }
            ).show(parentFragmentManager, "actions")
        })

        binding.rvServices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvServices.adapter = adapter
        adapter.submitList(data.toList())

        binding.fabAdd.setOnClickListener {
            AddServiceDialog { newItem ->
                data.add(newItem.copy(id = (data.maxOfOrNull { it.id } ?: 0) + 1))
                adapter.submitList(data.toList())
            }.show(parentFragmentManager, "add_service")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
