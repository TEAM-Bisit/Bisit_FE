package com.example.bisit.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bisit.R
import com.example.bisit.databinding.FragmentOwnerOnboardingBinding
import com.example.bisit.databinding.LayoutStepperBinding

class OwnerOnboardingFragment : Fragment() {

    private var _binding: FragmentOwnerOnboardingBinding? = null
    private val binding get() = _binding!!

    // Stepper UI의 ViewBinding에 접근하기 위한 변수
    private var _stepperBinding: LayoutStepperBinding? = null
    private val stepperBinding get() = _stepperBinding!!

    // 현재 진행 단계를 관리
    private var currentStep = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOwnerOnboardingBinding.inflate(inflater, container, false)
        // <include>된 레이아웃의 바인딩을 초기화
        _stepperBinding = LayoutStepperBinding.bind(binding.stepper.root)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Activity의 툴바 제목 변경
        (activity as? SignUpActivity)?.setToolbarTitle("첫 화면으로 돌아가기")

        // 2. '다음 단계' 버튼 리스너
        binding.btnNextStep.setOnClickListener {
            when (currentStep) {
                1 -> {
                    // 1단계 -> 2단계(매장 등록)로 이동
//                    replaceChildFragment(StoreInfoFragment.newInstance())
                    currentStep = 2
                }
                2 -> {
                    // 2단계 -> 3단계(매장 소개)로 이동
                    // replaceChildFragment(StoreIntroFragment.newInstance()) // 3단계 Fragment
                    // currentStep = 3
                }
                // ... (이후 4, 5단계)
            }
            // 단계가 변경되었으므로 공통 UI(Stepper, 이전 버튼)를 업데이트
            updateCommonUI()
        }

        // 3. '이전으로' 버튼 리스너
        binding.btnPrevious.setOnClickListener {
            // childFragmentManager의 백스택을 사용해 이전 프래그먼트로 돌아감
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
                currentStep--
                updateCommonUI()
            } else {
                // 백스택에 아무것도 없으면 (1단계라는 의미)
                // UserTypeFragment로 돌아가기
                (activity as? SignUpActivity)?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        // 4. 초기 자식 프래그먼트(1단계) 로드
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.owner_onboarding_nav_host, BusinessRegistrationFragment.newInstance())
                // 첫 프래그먼트는 백스택에 추가하지 않음
                .commit()
        }

        // 5. 초기 UI 상태 업데이트 (1단계 기준)
        updateCommonUI()
    }

    /**
     * 자식 프래그먼트를 교체하는 함수
     */
    private fun replaceChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.owner_onboarding_nav_host, fragment)
            .addToBackStack(null) // 뒤로가기를 위해 백스택에 추가
            .commit()
    }

    /**
     * 자식 프래그먼트가 '다음' 버튼을 활성화/비활성화할 수 있도록 하는 공용 함수
     */
    fun setNextButtonEnabled(isEnabled: Boolean) {
        binding.btnNextStep.isEnabled = isEnabled
    }

    /**
     * 현재 단계(currentStep)에 맞춰 공통 UI(Stepper, 이전 버튼)를 업데이트하는 함수
     */
    private fun updateCommonUI() {
        if (_binding == null || _stepperBinding == null) return // 뷰가 없을 땐 중단

        // 1. '이전으로' 버튼 표시 여부 (1단계에선 숨김)
        binding.btnPrevious.visibility = if (currentStep > 1) View.VISIBLE else View.GONE

        // 2. '다음 단계' 버튼은 항상 비활성화 (자식 프래그먼트가 직접 활성화해야 함)
        binding.btnNextStep.isEnabled = false

        // 3. Stepper UI 업데이트

        // 리소스 가져오기
        val activeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_active)
        val inactiveDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_inactive)
        val activeText = ContextCompat.getColor(requireContext(), R.color.stepper_text_active)
        val inactiveText = ContextCompat.getColor(requireContext(), R.color.stepper_text_inactive)

        // 아이콘/텍스트 뷰 리스트
        val allIcons = listOf(stepperBinding.step1Icon, stepperBinding.step2Icon, stepperBinding.step3Icon, stepperBinding.step4Icon, stepperBinding.step5Icon)
        val allTexts = listOf(stepperBinding.step1Text, stepperBinding.step2Text, stepperBinding.step3Text, stepperBinding.step4Text, stepperBinding.step5Text)

        // 아이콘 크기 (dimens.xml에 정의됨)
        val activeSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_active)
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_inactive)

        // 모든 아이콘과 텍스트를 우선 '비활성' 상태로 초기화
        allIcons.forEach { icon ->
            icon.setImageDrawable(inactiveDrawable)
            icon.layoutParams.width = inactiveSize
            icon.layoutParams.height = inactiveSize
            icon.requestLayout()
        }
        allTexts.forEach { text ->
            text.setTextColor(inactiveText)
        }

        // 현재 단계(currentStep)에 해당하는 아이콘과 텍스트만 '활성' 상태로 변경
        if (currentStep in 1..allIcons.size) {
            val activeIcon = allIcons[currentStep - 1]
            activeIcon.setImageDrawable(activeDrawable)
            activeIcon.layoutParams.width = activeSize
            activeIcon.layoutParams.height = activeSize
            activeIcon.requestLayout()

            allTexts[currentStep - 1].setTextColor(activeText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // 툴바 제목 원래대로 (예: "회원가입")
        (activity as? SignUpActivity)?.setToolbarTitle("회원가입")
        _stepperBinding = null
        _binding = null
    }
}