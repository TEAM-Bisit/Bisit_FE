package com.example.bisit.ui.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bisit.databinding.FragmentStoreCategoryBinding

class StoreCategoryFragment : Fragment() {

    private var _binding: FragmentStoreCategoryBinding? = null
    private val binding get() = _binding!!

    // 1. 어댑터 선언
    private lateinit var categoryAdapter: StoreCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 상태: 다음 단계 버튼 비활성화
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(false)

        setupSearchButton()
        setupRecyclerView() // 어댑터 설정 함수 호출
    }

    private fun setupSearchButton() {
        binding.etCategorySearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                binding.btnCategorySearch.isEnabled = s?.isNotBlank() == true
            }
        })

        // 검색 버튼 클릭 리스너
        binding.btnCategorySearch.setOnClickListener {
            val query = binding.etCategorySearch.text.toString()
            if (query.isNotBlank()) {
                performSearch(query)
            }
        }
    }

    private fun setupRecyclerView() {
        // 2. 어댑터 초기화 및 클릭 리스너 구현
        categoryAdapter = StoreCategoryAdapter { selectedItem ->
            // 리스트 아이템 클릭 시 동작
            binding.etCategorySearch.setText(selectedItem) // 선택한 항목을 입력창에 반영
            onCategorySelected() // 다음 단계 버튼 활성화
        }

        binding.rvCategoryResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun performSearch(query: String) {
        // 3. 검색 결과 업데이트 (임시 더미 데이터)
        val dummyResults = listOf(
            "디지털/전자 > 전자기기 서비스 > 컴퓨터 수리",
            "디지털/전자 > 전자기기 서비스 > 스마트폰 수리",
            "생활서비스 > 가전제품 > 에어컨 설치/수리",
            "전문서비스 > IT/소프트웨어 > 앱 개발"
        )

        binding.layoutSearchResultsBox.visibility = View.VISIBLE
        categoryAdapter.submitList(dummyResults)
    }

    private fun onCategorySelected() {
        // 최종 선택 완료 시 부모 프래그먼트의 '다음' 버튼을 파란색으로 활성화
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreCategoryFragment()
    }
}