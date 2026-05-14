package kr.bisit.app.ui.myPageOwner

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.TextView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.databinding.FragmentMyPageOwnerBinding
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.model.auth.AuthResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPageOwnerFragment : Fragment() {
    private var _binding: FragmentMyPageOwnerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageOwnerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoutLayout.setOnClickListener {
            performLogout()
        }

        binding.btnEditInfo.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageOwnerEditFragment)
        }

        binding.btnCouponIssue.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_ownerCouponManageFragment)
        }

        binding.leaveLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageLeaveFragment)
        }

        binding.centerLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageCenterFragment)
        }

        binding.announceLayout.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageAnnounceFragment)
        }

        binding.term1.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm1Fragment)
        }

        binding.term2.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm2Fragment)
        }

        binding.term3.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm3Fragment)
        }

        binding.term4.setOnClickListener {
            findNavController().navigate(R.id.action_myPageOwnerFragment_to_myPageTerm4Fragment)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshOnboarding()
    }

    fun refreshOnboarding() {
        val activity = requireActivity() as MainActivity

        if (!activity.isOnboardingActive()) {
            clearGuide(activity)
            return
        }

        if (activity.currentGuideStep != MainActivity.GuideStep.MY_TAB) return

        binding.btnCouponIssue.post {

            activity.showGlobalOverlay(
                targetView = binding.btnCouponIssue,
                shape = kr.bisit.app.ui.shop.HighlightOverlayView.HighlightShape.ROUNDED_RECT,
                radiusDp = 16f
            )

            showBigTextBelowTargetSimple(
                targetView = binding.btnCouponIssue,
                big = "고객님들에게 우리 가게의 쿠폰을\n발급할 수 있어요."
            )
        }
    }

    private fun showBigTextBelowTargetSimple(targetView: View, big: String) {
        val activity = requireActivity() as MainActivity
        val guideLayer = activity.getGlobalGuideLayer()
        guideLayer.removeAllViews()
        guideLayer.visibility = View.VISIBLE

        val bigText = TextView(requireContext()).apply {
            text = big
            setTextColor(android.graphics.Color.WHITE)
            setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 18f)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        guideLayer.addView(bigText)

        guideLayer.post {
            val layerLoc = IntArray(2)
            guideLayer.getLocationOnScreen(layerLoc)

            val r = android.graphics.Rect()
            targetView.getGlobalVisibleRect(r)

            val targetBottomLocal = r.bottom - layerLoc[1]

            bigText.x = dp(22f)
            bigText.y = targetBottomLocal + dp(24f)
            bigText.bringToFront()
        }
    }

    private fun clearGuide(activity: MainActivity) {
        val layer = activity.getGlobalGuideLayer()
        layer.removeAllViews()
        layer.visibility = View.GONE
        activity.hideGlobalOverlay()
    }

    private fun dp(value: Float): Float =
        android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            value,
            resources.displayMetrics
        )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performLogout() {
        RetrofitClient.getAuthApi(requireContext()).logout()
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "로그아웃되었습니다", Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.logout()
                    } else {
                        Toast.makeText(requireContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
