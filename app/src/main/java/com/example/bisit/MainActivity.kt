package com.example.bisit

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.bisit.databinding.ActivityMainBinding
import com.example.bisit.ui.shop.HighlightOverlayView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isOwner: Boolean = false
    private var onboardingEnabled: Boolean = false

    /* ===================== 온보딩 단계 ===================== */

    enum class GuideStep {
        TAB,
        EDIT_BUTTON,
        SERVICE_TAB,
        SERVICE_SCREEN,
        TODAY_TAB,
        TODAY_SCREEN,
        MY_TAB,
        DONE
    }

    var currentGuideStep: GuideStep = GuideStep.DONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupStatusBar()

        val userType = intent.getStringExtra("USER_TYPE")
        isOwner = userType == "owner"

        setupBottomNav(userType)
        setupNavGraph(userType)

        setupGuideTouch()
        setupSkipListener()

        if (isOwner && !isOnboardingCompleted()) {
            onboardingEnabled = true
            currentGuideStep = GuideStep.TAB
        }
    }

    /* ===================== 네비 설정 ===================== */

    private fun setupBottomNav(userType: String?) {
        binding.bottomNavView.menu.clear()

        if (userType == "owner") {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu_owner)
        } else {
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        }
    }

    private fun setupNavGraph(userType: String?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment

        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

        if (userType == "owner") {
            navGraph.setStartDestination(R.id.shopFragment)
        } else {
            navGraph.setStartDestination(R.id.customerCategoryFragment)
        }

        navController.graph = navGraph
        NavigationUI.setupWithNavController(binding.bottomNavView, navController)
    }

    /* ===================== 오버레이 터치 = 다음 단계 ===================== */

    private fun setupGuideTouch() {
        binding.globalOverlay.setOnClickListener {
            if (!onboardingEnabled) return@setOnClickListener
            goToNextStep()
        }
    }

    /* ===================== Skip 버튼 (Overlay 내부 버튼 사용) ===================== */

    private fun setupSkipListener() {
        binding.globalOverlay.setOnSkipClickListener {
            finishOnboarding()
            binding.bottomNavView.selectedItemId = R.id.shopFragment
        }
    }

    /* ===================== 단계 전환 ===================== */

    private fun goToNextStep() {

        when (currentGuideStep) {

            GuideStep.TAB -> {
                currentGuideStep = GuideStep.EDIT_BUTTON
            }

            GuideStep.EDIT_BUTTON -> {
                currentGuideStep = GuideStep.SERVICE_TAB
            }

            GuideStep.SERVICE_TAB -> {
                currentGuideStep = GuideStep.SERVICE_SCREEN
            }

            GuideStep.SERVICE_SCREEN -> {
                currentGuideStep = GuideStep.TODAY_TAB
                binding.bottomNavView.selectedItemId = R.id.todayReservFragment
            }

            GuideStep.TODAY_TAB -> {
                currentGuideStep = GuideStep.TODAY_SCREEN
            }

            GuideStep.TODAY_SCREEN -> {
                currentGuideStep = GuideStep.MY_TAB
                binding.bottomNavView.selectedItemId = R.id.myPageOwnerFragment
            }

            GuideStep.MY_TAB -> {
                finishOnboarding()
            }

            else -> finishOnboarding()
        }
    }

    /* ===================== 글로벌 오버레이 ===================== */

    fun showGlobalOverlay(
        targetView: View,
        guideText: String,
        shape: HighlightOverlayView.HighlightShape =
            HighlightOverlayView.HighlightShape.ROUNDED_RECT,
        radiusDp: Float = 12f
    ) {
        if (!onboardingEnabled) return

        val rect = Rect()
        targetView.getGlobalVisibleRect(rect)

        val rectF = RectF(rect)

        binding.globalOverlay.visibility = View.VISIBLE
        binding.globalGuideText.visibility = View.VISIBLE

        binding.globalOverlay.highlight(
            rect = rectF,
            shape = shape,
            radiusDp = radiusDp
        )

        binding.globalGuideText.text = guideText
        binding.globalGuideText.x = rect.left.toFloat()
        binding.globalGuideText.y = rect.bottom + 24f
    }

    fun hideGlobalOverlay() {
        binding.globalOverlay.visibility = View.GONE
        binding.globalGuideText.visibility = View.GONE
    }

    /* ===================== 온보딩 완료 ===================== */

    fun finishOnboarding() {
        currentGuideStep = GuideStep.DONE
        onboardingEnabled = false
        saveOnboardingCompleted()
        hideGlobalOverlay()
    }

    private fun isOnboardingCompleted(): Boolean {
        val prefs = getSharedPreferences("guide_pref", Context.MODE_PRIVATE)
        return prefs.getBoolean("owner_onboarding_done", false)
    }

    private fun saveOnboardingCompleted() {
        val prefs = getSharedPreferences("guide_pref", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("owner_onboarding_done", true).apply()
    }

    /* ===================== 상태바 ===================== */

    private fun setupStatusBar() {
        window.statusBarColor = android.graphics.Color.WHITE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }

    /* ===================== 로그인 이동 ===================== */

    fun logout() {
        binding.bottomNavView.visibility = View.GONE
        val navController = findNavController(R.id.nav_host_fragment)
        val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.authFragment)
        navController.setGraph(navGraph, null)
    }

    fun moveToHomeAfterLogin(userType: String) {
        binding.bottomNavView.visibility = View.VISIBLE
        setupNavGraph(userType)

        if (userType == "owner" && !isOnboardingCompleted()) {
            onboardingEnabled = true
            currentGuideStep = GuideStep.TAB
        }
    }
}
