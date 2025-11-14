package com.example.bisit.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.bisit.ui.auth.AuthActivity
import com.example.bisit.databinding.FragmentAuthBinding
import com.example.bisit.ui.login.LoginActivity
import com.example.bisit.ui.signUp.SignUpActivity

class AuthFragment : Fragment(), UserTypeDialog.UserTypeDialogListener {

    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val requiredTaps = 3
    private val tapTimeWindow: Long = 1000
    private val tapTimestamps = LongArray(requiredTaps)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logoImg.setOnClickListener {
            handleLogoTaps()
        }

        binding.signupText.setOnClickListener {
            val intent = Intent(requireContext(), SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }
    }

    private fun handleLogoTaps() {
        System.arraycopy(tapTimestamps, 1, tapTimestamps, 0, tapTimestamps.size - 1)
        tapTimestamps[tapTimestamps.size - 1] = System.currentTimeMillis()

        if (tapTimestamps[0] >= (System.currentTimeMillis() - tapTimeWindow)) {
            tapTimestamps.fill(0)
            showUserTypeDialog()
        }
    }

    private fun showUserTypeDialog() {
        val dialog = UserTypeDialog.newInstance()
        dialog.setListener(this)
        dialog.show(parentFragmentManager, "UserTypeDialog")
    }

    override fun onUserTypeSelected(userType: String) {
        (activity as? AuthActivity)?.navigateToMainActivity(userType)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = AuthFragment()
    }
}