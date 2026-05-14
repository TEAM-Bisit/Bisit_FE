package kr.bisit.app.ui.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.databinding.FragmentOnboardingDoneBinding

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
            (requireActivity() as MainActivity).finishOnboarding()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}