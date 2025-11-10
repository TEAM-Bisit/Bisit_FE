package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentShopNoticesBinding
import com.example.bisit.ui.shop.adapter.NoticeAdapter
import com.example.bisit.ui.shop.dialog.AddNoticeDialog
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import com.example.bisit.ui.shop.model.Notice

class ShopNoticesFragment : Fragment() {
    private var _binding: FragmentShopNoticesBinding? = null
    private val binding get() = _binding!!

    // 전역 변수로 이동
    private lateinit var adapter: NoticeAdapter

    private val data = mutableListOf(
        Notice(1, "개인 사정으로 휴무입니다.", "09/13 금요일은 개인 사정으로 휴무입니다.", "2025.09.13"),
        Notice(2, "광복절 휴무입니다.", "공휴일 운영안내: 일정 공지", "2025.08.15")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopNoticesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = NoticeAdapter(onMoreClick = { notice ->
            BottomActionSheet().show(parentFragmentManager, "actions")

            // 결과 수신 리스너 설정
            parentFragmentManager.setFragmentResultListener(
                BottomActionSheet.REQUEST_KEY,
                viewLifecycleOwner
            ) { _, bundle ->
                when (bundle.getString(BottomActionSheet.RESULT_ACTION)) {
                    BottomActionSheet.ACTION_DELETE -> {
                        ConfirmDialog("삭제하시겠어요?", onOk = {
                            val filtered = data.filterNot { it.id == notice.id }
                            data.clear()
                            data.addAll(filtered)
                            adapter.submitList(data.toList())
                        }).show(parentFragmentManager, "confirm")
                    }

                    BottomActionSheet.ACTION_EDIT -> {
                        AddNoticeDialog(prefill = notice) { updated ->
                            val idx = data.indexOfFirst { it.id == updated.id }
                            if (idx >= 0) {
                                data[idx] = updated
                                adapter.submitList(data.toList())
                            }
                        }.show(parentFragmentManager, "edit_notice")
                    }
                }
            }
        })

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = adapter
        adapter.submitList(data.toList())

        binding.fabAdd.setOnClickListener {
            AddNoticeDialog { newItem ->
                data.add(newItem.copy(id = (data.maxOfOrNull { it.id } ?: 0) + 1))
                adapter.submitList(data.toList())
            }.show(parentFragmentManager, "add_notice")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
