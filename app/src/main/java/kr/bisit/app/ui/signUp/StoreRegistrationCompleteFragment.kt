// StoreRegistrationCompleteFragment.kt
package kr.bisit.app.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kr.bisit.app.MainActivity
import kr.bisit.app.databinding.FragmentStoreRegistrationCompleteBinding

class StoreRegistrationCompleteFragment : Fragment() {

    private var _binding: FragmentStoreRegistrationCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStoreRegistrationCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnGoToManagement.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java).apply {
                // MainActivity에서 인식할 수 있도록 "owner" 전달
                putExtra("USER_TYPE", "owner")

                // 가입/등록 프로세스 스택을 제거하여 뒤로가기 방지
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StoreRegistrationCompleteFragment()
    }
}