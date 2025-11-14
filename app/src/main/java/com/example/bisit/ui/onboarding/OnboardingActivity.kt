package com.example.bisit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.bisit.databinding.ActivityOnboardingBinding
import com.example.bisit.ui.auth.AuthActivity
import com.example.bisit.ui.onboarding.OnboardingPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var adapter: OnboardingPagerAdapter

    // 페이지별 텍스트 리소스 배열
    private val titles = listOf(
        R.string.onboarding_title_1,
        R.string.onboarding_title_2,
        R.string.onboarding_title_3
    )
    private val subtitles = listOf(
        R.string.onboarding_subtitle_1,
        R.string.onboarding_subtitle_2,
        R.string.onboarding_subtitle_3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = OnboardingPagerAdapter(this)
        binding.viewPagerOnboarding.adapter = adapter

        // ViewPager2와 TabLayout(인디케이터) 연결
        TabLayoutMediator(binding.tabLayoutIndicator, binding.viewPagerOnboarding) { tab, position ->
            // 점만 표시
        }.attach()

        // 페이지 변경 감지
        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // 텍스트 업데이트
                binding.tvOnboardingTitle.setText(titles[position])
                binding.tvOnboardingSubtitle.setText(subtitles[position])

                if (position == adapter.itemCount - 1) {
                    // 마지막 페이지
                    binding.btnNext.text = getString(R.string.onboarding_start)
                } else {
                    binding.btnNext.text = getString(R.string.onboarding_next)
                }
            }
        })

        // 초기 텍스트 설정 (0번째 페이지)
        binding.tvOnboardingTitle.setText(titles[0])
        binding.tvOnboardingSubtitle.setText(subtitles[0])

        // 버튼 클릭 리스너
        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPagerOnboarding.currentItem
            if (currentItem < adapter.itemCount - 1) {
                binding.viewPagerOnboarding.currentItem = currentItem + 1
            } else {
                val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                prefs.edit().putBoolean("is_first_run", false).apply()

                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
        }
    }
}