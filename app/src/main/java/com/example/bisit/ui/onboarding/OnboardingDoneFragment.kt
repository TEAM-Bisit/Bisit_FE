package com.example.bisit.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.databinding.FragmentOnboardingDoneBinding

class OnboardingDoneFragment : Fragment(R.layout.fragment_onboarding_done) {

    private var _binding: FragmentOnboardingDoneBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingDoneBinding.bind(view)

        (requireActivity() as MainActivity).apply {
            hideGlobalOverlay()
            getGlobalGuideLayer().removeAllViews()
            getGlobalGuideLayer().visibility = View.GONE
        }

        binding.btnStart.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.goToShopTab()
            findNavController().popBackStack(R.id.shopFragment, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}