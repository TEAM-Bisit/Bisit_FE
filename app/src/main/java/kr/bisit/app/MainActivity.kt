package kr.bisit.app

import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import kr.bisit.app.databinding.ActivityMainBinding
import kr.bisit.app.ui.reservList.ReservListFragment
import kr.bisit.app.ui.shop.HighlightOverlayView
import kr.bisit.app.ui.shop.ShopBasicFragment
import kr.bisit.app.ui.shop.ShopFragment
import kr.bisit.app.ui.shop.ShopServicesFragment
import kr.bisit.app.ui.todayReserv.TodayReservFragment
import kr.bisit.app.ui.myPageOwner.MyPageOwnerFragment
import kr.bisit.app.ui.myPageOwner.OwnerCouponManageFragment
import kr.bisit.app.ui.onboarding.OnboardingDoneFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var globalOverlay: HighlightOverlayView
    private lateinit var globalGuideTextLayer: FrameLayout

    private var isOwner = false
    private var onboardingEnabled = false
    private var isTransitioning = false

    enum class GuideStep {
        TAB,
        EDIT_BUTTON,
        SERVICE_TAB,
        SERVICE_SCREEN,
        SERVICE_MODAL_GUIDE,
        SERVICE_MODAL_OPEN,
        TODAY_TAB,
        TODAY_APPROVE,
        TODAY_STATUS,
        TODAY_CONFIRM,
        TODAY_DETAIL,
        MY_TAB,
        MY_COUPON,
        DONE
    }

    var currentGuideStep: GuideStep = GuideStep.DONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val decorView = window.decorView as ViewGroup

        globalOverlay = HighlightOverlayView(this)
        decorView.addView(
            globalOverlay,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        globalOverlay.elevation = 9999f
        globalOverlay.visibility = View.GONE

        globalGuideTextLayer = FrameLayout(this)
        globalGuideTextLayer.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        decorView.addView(globalGuideTextLayer)
        globalGuideTextLayer.elevation = 10000f

        /* =============================== */

        globalOverlay.setOnNextClickListener {
            if (!onboardingEnabled) return@setOnNextClickListener
            goToNextStep()
        }

        setupStatusBar()

        val userType = intent.getStringExtra("USER_TYPE")
        isOwner = userType == "owner"

        setupBottomNav(userType)
        setupNavGraph(userType)
        setupSkipListener()

        if (isOwner && !hasShownOnboarding()) {
            onboardingEnabled = true
            currentGuideStep = GuideStep.TAB
            binding.root.post { refreshCurrentFragmentOverlay() }
        } else {
            onboardingEnabled = false
            currentGuideStep = GuideStep.DONE
            hideGlobalOverlay()
            globalGuideTextLayer.removeAllViews()
            globalGuideTextLayer.visibility = View.GONE
        }
    }

    /* ========================================================= */

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

    private fun setupSkipListener() {
        globalOverlay.setOnSkipClickListener {
            finishOnboarding()
            binding.bottomNavView.selectedItemId = R.id.shopFragment
        }
    }

    /* ========================================================= */

    private fun goToNextStep() {

        if (isTransitioning) return
        isTransitioning = true
        binding.root.postDelayed({
            isTransitioning = false
        }, 250)

        globalGuideTextLayer.removeAllViews()
        globalGuideTextLayer.visibility = View.GONE
        hideGlobalOverlay()

        when (currentGuideStep) {
            GuideStep.TAB -> currentGuideStep = GuideStep.EDIT_BUTTON
            GuideStep.EDIT_BUTTON -> currentGuideStep = GuideStep.SERVICE_TAB

            GuideStep.SERVICE_TAB -> {
                currentGuideStep = GuideStep.SERVICE_SCREEN
                binding.bottomNavView.selectedItemId = R.id.shopFragment
                binding.root.post { refreshCurrentFragmentOverlay() }
                return
            }

            GuideStep.SERVICE_SCREEN ->
                currentGuideStep = GuideStep.SERVICE_MODAL_GUIDE

            GuideStep.SERVICE_MODAL_GUIDE ->
                currentGuideStep = GuideStep.SERVICE_MODAL_OPEN

            GuideStep.SERVICE_MODAL_OPEN -> return

            GuideStep.TODAY_TAB -> {
                currentGuideStep = GuideStep.TODAY_APPROVE
                binding.bottomNavView.selectedItemId =
                    R.id.todayReservFragment
                return
            }

            GuideStep.TODAY_APPROVE ->
                currentGuideStep = GuideStep.TODAY_STATUS

            GuideStep.TODAY_STATUS ->
                currentGuideStep = GuideStep.TODAY_CONFIRM

            GuideStep.TODAY_CONFIRM -> {
                currentGuideStep = GuideStep.TODAY_DETAIL
                binding.bottomNavView.selectedItemId = R.id.reservListFragment
                binding.root.post { refreshCurrentFragmentOverlay() }
                return
            }

            GuideStep.TODAY_DETAIL -> {
                currentGuideStep = GuideStep.MY_TAB
                binding.bottomNavView.selectedItemId = R.id.myPageOwnerFragment
                binding.root.post { refreshCurrentFragmentOverlay() }
                return
            }

            GuideStep.MY_TAB -> {
                currentGuideStep = GuideStep.MY_COUPON
                binding.bottomNavView.selectedItemId = R.id.myPageOwnerFragment
                binding.root.post {
                    val navController = findNavController(R.id.nav_host_fragment)
                    navController.navigate(R.id.action_myPageOwnerFragment_to_ownerCouponManageFragment)
                    refreshCurrentFragmentOverlay()
                }
                return
            }

            GuideStep.MY_COUPON -> {
                finishOnboarding()
                binding.root.post {
                    findNavController(R.id.nav_host_fragment)
                        .navigate(R.id.action_global_onboardingDoneFragment)
                }
                return
            }

            else -> return
        }

        refreshCurrentFragmentOverlay()
    }

    /* ========================================================= */

    fun refreshCurrentFragmentOverlay() {

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment

        val fragments = navHostFragment.childFragmentManager.fragments

        fragments.forEach { fragment ->
            when (fragment) {
                is ShopFragment -> {
                    fragment.refreshOnboarding()

                    fragment.childFragmentManager.fragments.forEach { child ->
                        when (child) {
                            is ShopBasicFragment -> child.refreshOnboarding()
                            is ShopServicesFragment -> child.refreshOnboarding()
                            is TodayReservFragment -> child.refreshOnboarding()
                            is MyPageOwnerFragment -> { fragment.refreshOnboarding() }
                            is OwnerCouponManageFragment -> { fragment.refreshOnboarding() }
                            is OnboardingDoneFragment -> { }
                        }
                    }
                }

                is TodayReservFragment -> {
                    fragment.refreshOnboarding()
                }

                is ReservListFragment -> {
                    fragment.refreshOnboarding()
                }
            }
        }
    }

    fun isOnboardingActive(): Boolean = onboardingEnabled

    fun goToShopTab() {
        binding.bottomNavView.selectedItemId = R.id.shopFragment
    }

    fun getGlobalGuideLayer(): FrameLayout {
        return globalGuideTextLayer
    }

    /* ========================================================= */

    fun showGlobalOverlayMultiple(
        rects: List<RectF>,
        shape: HighlightOverlayView.HighlightShape,
        radiusDp: Float
    ) {
        globalOverlay.visibility = View.VISIBLE
        globalOverlay.highlightMultiple(rects, shape, radiusDp)
    }

    fun showGlobalOverlay(
        targetView: View,
        shape: HighlightOverlayView.HighlightShape,
        radiusDp: Float
    ) {
        val rect = Rect()
        targetView.getGlobalVisibleRect(rect)
        val rectF = RectF(rect)

        globalOverlay.visibility = View.VISIBLE
        globalOverlay.highlight(rectF, shape, radiusDp)
    }

    fun highlightBottomNavItem(index: Int) {

        val menuView = binding.bottomNavView.getChildAt(0) as ViewGroup
        val itemView = menuView.getChildAt(index)

        itemView.post {

            val rect = Rect()
            itemView.getGlobalVisibleRect(rect)

            val size = 72f * resources.displayMetrics.density
            val cx = rect.centerX()
            val cy = rect.centerY()

            val circleRect = RectF(
                cx - size / 2,
                cy - size / 2,
                cx + size / 2,
                cy + size / 2
            )

            globalOverlay.visibility = View.VISIBLE
            globalOverlay.highlight(
                circleRect,
                HighlightOverlayView.HighlightShape.CIRCLE,
                0f
            )
        }
    }

    fun getBottomNavHighlightRect(index: Int, sizeDp: Float = 72f): RectF? {
        val menuView = binding.bottomNavView.getChildAt(0) as? ViewGroup ?: return null
        val itemView = menuView.getChildAt(index) ?: return null

        val rect = Rect()
        itemView.getGlobalVisibleRect(rect)

        val size = sizeDp * resources.displayMetrics.density
        val cx = rect.centerX()
        val cy = rect.centerY()

        return RectF(
            cx - size / 2f,
            cy - size / 2f,
            cx + size / 2f,
            cy + size / 2f
        )
    }

    private val onboardingPrefs by lazy {
        getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
    }

    private val KEY_OWNER_ONBOARDING_SHOWN = "owner_onboarding_shown"

    private fun hasShownOnboarding(): Boolean =
        onboardingPrefs.getBoolean(KEY_OWNER_ONBOARDING_SHOWN, false)

    private fun markOnboardingShown() {
        onboardingPrefs.edit().putBoolean(KEY_OWNER_ONBOARDING_SHOWN, true).apply()
    }

    fun hideGlobalOverlay() {
        globalOverlay.visibility = View.GONE
        globalOverlay.clearHighlight()
    }

    fun showDimOnlyOverlay() {
        globalOverlay.visibility = View.VISIBLE
        globalOverlay.clearHighlight()
    }

    fun onboardingNext() {
        if (!onboardingEnabled) return
        goToNextStep()
    }

    fun showGlobalOverlayMixed(
        specs: List<HighlightOverlayView.HighlightSpec>
    ) {
        globalOverlay.visibility = View.VISIBLE

        val converted = specs.map { spec ->
            spec.copy(radiusPx = dpToPx(spec.radiusPx))
        }

        globalOverlay.highlightMixed(converted)
    }

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density

    fun finishOnboarding() {
        currentGuideStep = GuideStep.DONE
        onboardingEnabled = false
        hideGlobalOverlay()
        globalGuideTextLayer.removeAllViews()

        if (isOwner) markOnboardingShown()
    }

    /* ========================================================= */

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

    fun logout() {
        binding.bottomNavView.visibility = View.GONE
        val navController = findNavController(R.id.nav_host_fragment)
        val navGraph =
            navController.navInflater.inflate(R.navigation.nav_graph)
        navGraph.setStartDestination(R.id.authFragment)
        navController.setGraph(navGraph, null)
    }
}
