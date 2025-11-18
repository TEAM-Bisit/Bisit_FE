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
                    replaceChildFragment(StoreInfoFragment.newInstance())
                    currentStep = 2
                }
                2 -> {
                    // 2단계 -> 3단계(매장 소개)로 이동
                    // replaceChildFragment(StoreIntroFragment.newInstance())
                    // currentStep = 3
                }
                // ... (이후 4, 5단계)
            }
            updateCommonUI()
        }

        // 3. '이전으로' 버튼 리스너
        binding.btnPrevious.setOnClickListener {
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
                currentStep--
                updateCommonUI()
            } else {
                (activity as? SignUpActivity)?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        // 4. 초기 자식 프래그먼트(1단계) 로드
        if (savedInstanceState == null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.owner_onboarding_nav_host, BusinessRegistrationFragment.newInstance())
                .commit()
        }

        // 5. 초기 UI 상태 업데이트
        updateCommonUI()
    }

    private fun replaceChildFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.owner_onboarding_nav_host, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun setNextButtonEnabled(isEnabled: Boolean) {
        binding.btnNextStep.isEnabled = isEnabled
    }

    private fun updateCommonUI() {
        if (_binding == null || _stepperBinding == null) return

        // 1. '이전으로' 버튼 처리
        if (currentStep > 1) {
            binding.btnPrevious.visibility = View.VISIBLE
            binding.btnPrevious.isEnabled = true
        } else {
            binding.btnPrevious.visibility = View.INVISIBLE
            binding.btnPrevious.isEnabled = false
        }

        // 2. '다음 단계' 버튼 초기화
        binding.btnNextStep.isEnabled = false

        // 3. Stepper UI 업데이트

        // 리소스 가져오기
        val passedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_passed) // 꽉 찬 파란원
        val activeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_active) // 링 모양
        val inactiveDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_inactive) // 작은 회색원

        val activeText = ContextCompat.getColor(requireContext(), R.color.stepper_text_active)
        val inactiveText = ContextCompat.getColor(requireContext(), R.color.stepper_text_inactive)

        val barActiveColor = ContextCompat.getColor(requireContext(), R.color.stepper_bar_active)
        val barInactiveColor = ContextCompat.getColor(requireContext(), R.color.stepper_bar_inactive)

        // 뷰 리스트
        val allIcons = listOf(stepperBinding.step1Icon, stepperBinding.step2Icon, stepperBinding.step3Icon, stepperBinding.step4Icon, stepperBinding.step5Icon)
        val allTexts = listOf(stepperBinding.step1Text, stepperBinding.step2Text, stepperBinding.step3Text, stepperBinding.step4Text, stepperBinding.step5Text)
        val allBars = listOf(stepperBinding.step1Bar, stepperBinding.step2Bar, stepperBinding.step3Bar, stepperBinding.step4Bar)

        // ★ 사이즈 리소스 가져오기 (dimens.xml에서 정의된 18dp, 8dp)
        val activeSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_active) // 18dp
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_inactive) // 8dp

        // 비활성/지나온 단계 아이콘에 적용할 패딩 계산: (18dp - 8dp) / 2 = 5dp
        val paddingForSmallIcons = (activeSize - inactiveSize) / 2

        // 루프를 돌며 상태에 따라 UI 업데이트
        for (i in allIcons.indices) {
            val stepNum = i + 1
            val icon = allIcons[i]
            val text = allTexts[i]

            // ★ 중요: 뷰의 틀(Frame) 크기는 항상 '큰 사이즈(18dp)'로 고정하여 레이아웃 흔들림 방지
            icon.layoutParams.width = activeSize
            icon.layoutParams.height = activeSize
            icon.requestLayout()

            if (stepNum < currentStep) {
                // [1] 지나온 단계 (Passed) -> 꽉 찬 파란원 (8dp)
                // ★ 패딩을 주어 시각적으로만 8dp로 만듦
                icon.setPadding(paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons)
                icon.setImageDrawable(passedDrawable)
                text.setTextColor(activeText)

                // 해당 단계 뒤의 바(Bar)도 파란색으로 변경
                if (i < allBars.size) {
                    allBars[i].setBackgroundColor(barActiveColor)
                }

            } else if (stepNum == currentStep) {
                // [2] 현재 단계 (Active) -> 링 모양 (18dp)
                icon.setPadding(0, 0, 0, 0) // 패딩 제거
                icon.setImageDrawable(activeDrawable)
                text.setTextColor(activeText)

                // 현재 단계 뒤의 바는 아직 회색
                if (i < allBars.size) {
                    allBars[i].setBackgroundColor(barInactiveColor)
                }

            } else {
                // [3] 미래 단계 (Inactive) -> 작은 회색원 (8dp)
                // ★ 패딩을 주어 시각적으로만 8dp로 만듦
                icon.setPadding(paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons)
                icon.setImageDrawable(inactiveDrawable)
                text.setTextColor(inactiveText)

                if (i < allBars.size) {
                    allBars[i].setBackgroundColor(barInactiveColor)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? SignUpActivity)?.setToolbarTitle("회원가입")
        _stepperBinding = null
        _binding = null
    }
}