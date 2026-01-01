package com.example.bisit.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.shop.ShopIndustryRequest
import com.example.bisit.data.model.shop.ShopIndustryResponse
import com.example.bisit.databinding.FragmentStoreCategoryBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoreCategoryFragment : Fragment() {

    private var _binding: FragmentStoreCategoryBinding? = null
    private val binding get() = _binding!!

    private val signUpViewModel: SignUpViewModel by activityViewModels()

    // 현재 선택된 카테고리를 저장할 변수
    private var selectedCategory: String? = null
    // 선택된 뷰를 관리하기 위함 (시각적 피드백용)
    private var selectedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 초기 상태: 다음 버튼 비활성화
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(false)

        setupCategoryListeners()
    }

    private fun setupCategoryListeners() {
        // 각 카테고리 뷰와 서버에 보낼 값 매핑
        val categoryMap = mapOf(
            binding.categoryHome to "LIVING",
            binding.categoryIt to "IT_ELECTRONICS",
            binding.categoryFix to "REPAIR_INSTALLATION",
            binding.categoryCar to "VEHICLE_MANAGEMENT",
            binding.categoryHealth to "HEALTHCARE",
            binding.categoryOffice to "OFFICE_MANAGEMENT",
            binding.categoryCamera to "PHOTO_EVENT",
            binding.categoryEdu to "EDUCATION"
        )

        categoryMap.forEach { (view, categoryName) ->
            view.setOnClickListener {
                handleCategorySelection(view, categoryName)
            }
        }
    }

    private fun handleCategorySelection(view: View, categoryName: String) {
        // 이전에 선택된 뷰의 배경을 원래대로 (필요 시 bg_edit_box 등으로 변경 가능)
        selectedView?.setBackgroundResource(android.R.color.transparent)

        // 현재 선택된 뷰 강조 (테두리 있는 배경 등으로 교체 권장)
        view.setBackgroundResource(R.drawable.bg_category_selected) // 미리 만들어둔 선택용 drawable

        selectedCategory = categoryName
        selectedView = view

        // 카테고리가 선택되었으므로 다음 버튼 활성화
        (parentFragment as? OwnerOnboardingFragment)?.setNextButtonEnabled(true)
    }

    fun saveIndustryAndNext(onSuccess: () -> Unit) {
        val shopId = signUpViewModel.shopId.value ?: 2 // 테스트용 2번
        val categoryCode = selectedCategory ?: return

        val request = ShopIndustryRequest(category = categoryCode)
        val api = RetrofitClient.getStoreApi(requireContext())

        api.updateIndustry(shopId, request).enqueue(object : Callback<ShopIndustryResponse> {
            override fun onResponse(call: Call<ShopIndustryResponse>, response: Response<ShopIndustryResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess() // 성공 시 다음 단계(영업시간)로 이동
                } else {
                    Toast.makeText(context, "업종 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ShopIndustryResponse>, t: Throwable) {
                Toast.makeText(context, "네트워크 통신 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreCategoryFragment()
    }
}