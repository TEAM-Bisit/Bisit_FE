package com.example.bisit.ui.signUp

import android.animation.ObjectAnimator // ★ 애니메이션용 Import
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator // ★ 애니메이션용 Import
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
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

    private val signUpViewModel: SignUpViewModel by activityViewModels()

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
        // OwnerOnboardingFragment.kt 의 onViewCreated 내부

        binding.btnNextStep.setOnClickListener {
            // 현재 표시되고 있는 자식 프래그먼트를 가져옵니다.
            val currentFragment = childFragmentManager.findFragmentById(R.id.owner_onboarding_nav_host)

            when (currentStep) {
                1 -> {
                    // [1단계 -> 2단계] 사업자 인증 완료 후 이동
                    replaceChildFragment(StoreInfoFragment.newInstance())
                    currentStep = 2
                    updateCommonUI()
                }

                2 -> {
                    // [2단계 -> 3단계] 매장 정보 등록 API 호출
                    if (currentFragment is StoreInfoFragment) {
                        currentFragment.registerStoreAndNext { shopId ->
                            // API 성공 시 shopId 저장 후 이동
                            signUpViewModel.setShopId(shopId)
                            replaceChildFragment(StoreIntroFragment.newInstance())
                            currentStep = 3
                            updateCommonUI()
                        }
                    }
                }

                3 -> {
                    // [3단계 -> 4단계] ★ 매장 소개 및 사진 업로드 API 호출
                    if (currentFragment is StoreIntroFragment) {
                        currentFragment.uploadDataAndNext {
                            // 사진 업로드 및 소개 등록 성공 시 이동
                            replaceChildFragment(StoreCategoryFragment.newInstance())
                            currentStep = 4
                            updateCommonUI()
                        }
                    }
                }

                4 -> {
                    // [4단계 -> 5단계] 업종(카테고리) 선택 후 이동
                    // (이 단계도 API가 필요하다면 위 단계들처럼 콜백 구조로 수정해야 합니다)
                    if (currentFragment is StoreCategoryFragment) {
                        currentFragment.saveIndustryAndNext {
                            replaceChildFragment(StoreHoursFragment.newInstance())
                            currentStep = 5
                            updateCommonUI()
                        }
                    }
                }

                5 -> {
                    if (currentFragment is StoreHoursFragment) {
                        currentFragment.saveAllHoursData {
                            // 모든 저장이 성공하면 다음 화면으로 이동
                            replaceChildFragment(StoreRegistrationCompleteFragment.newInstance())
                            currentStep = 6
                            updateCommonUI()
                        }
                    }
                }
            }
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
            val startDest = activity?.intent?.getStringExtra("START_DESTINATION")

            if (startDest == "OWNER_INTRO") { // 테스트용
                currentStep = 5

                signUpViewModel.setShopId(2)

                childFragmentManager.beginTransaction()
                    .replace(R.id.owner_onboarding_nav_host, StoreHoursFragment.newInstance())
                    .commit()
            } else {
                // 기본 1단계 시작
                currentStep = 1
                childFragmentManager.beginTransaction()
                    .replace(R.id.owner_onboarding_nav_host, BusinessRegistrationFragment.newInstance())
                    .commit()
            }
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

        if (currentStep == 6) {
            binding.stepper.root.visibility = View.GONE
            binding.btnPrevious.visibility = View.GONE
            binding.btnNextStep.visibility = View.GONE
            (activity as? SignUpActivity)?.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
            return
        }

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
        val passedDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_passed)
        val activeDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_active)
        val inactiveDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.stepper_circle_inactive)

        val activeText = ContextCompat.getColor(requireContext(), R.color.stepper_text_active)
        val inactiveText = ContextCompat.getColor(requireContext(), R.color.stepper_text_inactive)

        // 뷰 리스트
        val allIcons = listOf(stepperBinding.step1Icon, stepperBinding.step2Icon, stepperBinding.step3Icon, stepperBinding.step4Icon, stepperBinding.step5Icon)
        val allTexts = listOf(stepperBinding.step1Text, stepperBinding.step2Text, stepperBinding.step3Text, stepperBinding.step4Text, stepperBinding.step5Text)
        // ★ ProgressBar 리스트 (XML에서 View -> ProgressBar로 변경했으므로 자동으로 인식됨)
        val allBars = listOf(stepperBinding.step1Bar, stepperBinding.step2Bar, stepperBinding.step3Bar, stepperBinding.step4Bar)

        // 사이즈 리소스 가져오기
        val activeSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_active) // 18dp
        val inactiveSize = resources.getDimensionPixelSize(R.dimen.stepper_icon_size_inactive) // 8dp
        val paddingForSmallIcons = (activeSize - inactiveSize) / 2

        // [A] 아이콘 및 텍스트 업데이트
        for (i in allIcons.indices) {
            val stepNum = i + 1
            val icon = allIcons[i]
            val text = allTexts[i]

            // 프레임 크기 고정 (흔들림 방지)
            icon.layoutParams.width = activeSize
            icon.layoutParams.height = activeSize
            icon.requestLayout()

            if (stepNum < currentStep) {
                // [1] 지나온 단계 (Passed) -> 작은 사이즈(8dp)
                icon.setPadding(paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons)
                icon.setImageDrawable(passedDrawable)
                text.setTextColor(activeText)

            } else if (stepNum == currentStep) {
                // [2] 현재 단계 (Active) -> 큰 사이즈(18dp)
                icon.setPadding(0, 0, 0, 0) // 패딩 제거
                icon.setImageDrawable(activeDrawable)
                text.setTextColor(activeText)

            } else {
                // [3] 미래 단계 (Inactive) -> 작은 사이즈(8dp)
                icon.setPadding(paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons, paddingForSmallIcons)
                icon.setImageDrawable(inactiveDrawable)
                text.setTextColor(inactiveText)
            }
        }

        // [B] 구분선(Bar) 애니메이션 업데이트
        for (i in allBars.indices) {
            val bar = allBars[i]
            // 해당 Bar가 채워져야 하는 목표 단계 (Bar 0은 Step 2가 될 때 채워짐)
            val targetStep = i + 2

            if (currentStep >= targetStep) {
                // 채워져야 하는 상태
                if (bar.progress < 100) {
                    // 아직 안 채워져 있다면 애니메이션 실행 (스르륵~)
                    ObjectAnimator.ofInt(bar, "progress", 0, 100).apply {
                        duration = 500 // 0.5초 동안
                        interpolator = DecelerateInterpolator()
                        start()
                    }
                } else {
                    // 이미 채워져 있다면 유지
                    bar.progress = 100
                }
            } else {
                // 비워져야 하는 상태 (뒤로가기 시)
                bar.progress = 0
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