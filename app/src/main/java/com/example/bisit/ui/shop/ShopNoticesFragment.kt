package com.example.bisit.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentShopNoticesBinding
import com.example.bisit.ui.shop.adapter.NoticeAdapter
import com.example.bisit.ui.shop.dialog.AddNoticeDialog
import com.example.bisit.ui.shop.dialog.BottomActionSheet
import com.example.bisit.ui.shop.dialog.ConfirmDialog
import com.example.bisit.ui.shop.model.Notice
import com.example.bisit.ui.shop.register.ShopRegisterViewModelFactory
import com.example.bisit.ui.todayReserv.dialog.SortOptionDialog
import kotlinx.coroutines.launch

class ShopNoticesFragment : Fragment() {

    private var _binding: FragmentShopNoticesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoticeAdapter

    /** shopId 제공용 */
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext())
    }

    /** 공지사항 전용 ViewModel */
    private val noticeViewModel: ShopNoticeViewModel by viewModels {
        ShopNoticeViewModelFactory(requireContext())
    }

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

        setupRecyclerView()
        observeViewModel()
        setupSort()
        setupAddNotice()
    }

    /** ================= RecyclerView ================= */

    private fun setupRecyclerView() {
        adapter = NoticeAdapter { notice ->
            BottomActionSheet().show(parentFragmentManager, "actions")

            parentFragmentManager.setFragmentResultListener(
                BottomActionSheet.REQUEST_KEY,
                viewLifecycleOwner
            ) { _, bundle ->
                when (bundle.getString(BottomActionSheet.RESULT_ACTION)) {

                    BottomActionSheet.ACTION_DELETE -> {
                        ConfirmDialog("삭제하시겠어요?") {
                            val shopId =
                                shopRegisterViewModel.shopId.value ?: return@ConfirmDialog
                            noticeViewModel.deleteNotice(shopId, notice.id)
                        }.show(parentFragmentManager, "confirm")
                    }

                    BottomActionSheet.ACTION_EDIT -> {
                        AddNoticeDialog(prefill = notice) { updated ->
                            val shopId =
                                shopRegisterViewModel.shopId.value ?: return@AddNoticeDialog

                            noticeViewModel.updateNotice(
                                shopId = shopId,
                                noticeId = updated.id,
                                title = updated.title,
                                content = updated.content
                            )
                        }.show(parentFragmentManager, "edit_notice")
                    }
                }
            }
        }

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = adapter
    }

    /** ================= ViewModel Observe ================= */

    private fun observeViewModel() {
        // shopId 수신 → 공지사항 로드
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                if (shopId != null) {
                    noticeViewModel.loadNotices(shopId)
                }
            }
        }

        // 공지사항 리스트 반영
        viewLifecycleOwner.lifecycleScope.launch {
            noticeViewModel.notices.collect { list ->
                adapter.submitList(
                    list.map {
                        Notice(
                            id = it.id,
                            title = it.title,
                            content = it.content,
                            date = it.createdAt.substring(0, 10)
                        )
                    }
                )
            }
        }
    }

    /** ================= 정렬 ================= */

    private fun setupSort() {
        binding.tvSortLabel.setOnClickListener {
            SortOptionDialog(
                currentSort = noticeViewModel.sortType.value
            ) { selected ->
                val shopId =
                    shopRegisterViewModel.shopId.value ?: return@SortOptionDialog
                noticeViewModel.changeSort(shopId, selected)
            }.show(parentFragmentManager, "sort_option")
        }
    }

    /** ================= 공지 추가 ================= */

    private fun setupAddNotice() {
        binding.fabAdd.setOnClickListener {
            AddNoticeDialog { newItem ->
                val shopId =
                    shopRegisterViewModel.shopId.value ?: return@AddNoticeDialog

                noticeViewModel.createNotice(
                    shopId = shopId,
                    title = newItem.title,
                    content = newItem.content
                )
            }.show(parentFragmentManager, "add_notice")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
