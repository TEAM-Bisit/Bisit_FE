package kr.bisit.app.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kr.bisit.app.databinding.FragmentShopNoticesBinding
import kr.bisit.app.ui.shop.adapter.NoticeAdapter
import kr.bisit.app.ui.shop.dialog.AddNoticeDialog
import kr.bisit.app.ui.shop.dialog.BottomActionSheet
import kr.bisit.app.ui.shop.dialog.ConfirmDialog
import kr.bisit.app.ui.todayReserv.dialog.SortOptionDialog
import kotlinx.coroutines.launch

class ShopNoticesFragment : Fragment() {

    private var _binding: FragmentShopNoticesBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoticeAdapter

    /** shopId 제공용 */
    private val shopRegisterViewModel: ShopRegisterViewModel by activityViewModels {
        ShopRegisterViewModelFactory(requireContext().applicationContext)
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

    /* ================= RecyclerView ================= */

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
                        AddNoticeDialog(
                            prefillTitle = notice.title,
                            prefillContent = notice.content
                        ) { title, content ->
                            val shopId =
                                shopRegisterViewModel.shopId.value ?: return@AddNoticeDialog

                            noticeViewModel.updateNotice(
                                shopId = shopId,
                                noticeId = notice.id,
                                title = title,
                                content = content
                            )
                        }.show(parentFragmentManager, "edit_notice")
                    }
                }
            }
        }

        binding.rvNotices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotices.adapter = adapter
    }

    /* ================= ViewModel Observe ================= */

    private fun observeViewModel() {

        // shopId 수신 → 공지 조회
        viewLifecycleOwner.lifecycleScope.launch {
            shopRegisterViewModel.shopId.collect { shopId ->
                shopId ?: return@collect
                noticeViewModel.loadNotices(shopId)
            }
        }

        // 공지 리스트 반영 (API 모델 그대로)
        viewLifecycleOwner.lifecycleScope.launch {
            noticeViewModel.notices.collect { list ->
                adapter.submitList(list)
            }
        }
    }

    /* ================= 정렬 ================= */

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

    /* ================= 공지 추가 ================= */

    private fun setupAddNotice() {
        binding.fabAdd.setOnClickListener {
            AddNoticeDialog { title, content ->
                val shopId =
                    shopRegisterViewModel.shopId.value ?: return@AddNoticeDialog

                noticeViewModel.createNotice(
                    shopId = shopId,
                    title = title,
                    content = content
                )
            }.show(parentFragmentManager, "add_notice")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
