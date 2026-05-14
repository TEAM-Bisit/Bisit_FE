package kr.bisit.app.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kr.bisit.app.MainActivity
import kr.bisit.app.R
import kr.bisit.app.data.api.RetrofitClient
import kr.bisit.app.data.api.TokenManager
import kr.bisit.app.data.model.auth.ReissueRequest
import kr.bisit.app.data.model.auth.ReissueResponse
import kr.bisit.app.data.model.member.MemberRoleResponse
import kr.bisit.app.databinding.FragmentUserTypeBinding
import kr.bisit.app.data.model.member.MemberRoleRequest
import kr.bisit.app.ui.dialog.CommonInfoDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private enum class UserType {
    CUSTOMER, OWNER
}

class UserTypeFragment : Fragment() {

    private var _binding: FragmentUserTypeBinding? = null
    private val binding get() = _binding!!

    private var selectedUserType: UserType? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateSelectionView()

        binding.cardViewCustomer.setOnClickListener {
            selectedUserType = UserType.CUSTOMER
            updateSelectionView()
            binding.btnNext.isEnabled = true
        }

        binding.cardViewOwner.setOnClickListener {
            selectedUserType = UserType.OWNER
            updateSelectionView()
            binding.btnNext.isEnabled = true
        }

        binding.btnNext.setOnClickListener {
            val selectedRole = selectedUserType ?: return@setOnClickListener

            // 서버에 역할 업데이트 요청 실행
            updateMemberRoleOnServer(selectedRole)
        }
    }

    private fun updateSelectionView() {

        val selectedStrokeColor = ContextCompat.getColor(requireContext(), R.color.userType_selected_blue) // #4076FF
        val selectedBackgroundColor = ContextCompat.getColor(requireContext(), R.color.userType_selected_background) // #EBF1FF
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.userType_selected_blue) // #4076FF

        val defaultStrokeColor = ContextCompat.getColor(requireContext(), R.color.gray_300)
        val defaultBackgroundColor = ContextCompat.getColor(requireContext(), R.color.white)
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.black)

        val isCustomerSelected = (selectedUserType == UserType.CUSTOMER)
        binding.cardViewCustomer.apply {
            isChecked = isCustomerSelected
            strokeColor = if (isCustomerSelected) selectedStrokeColor else defaultStrokeColor
            setCardBackgroundColor(if (isCustomerSelected) selectedBackgroundColor else defaultBackgroundColor)
            binding.textView5.setTextColor(if (isCustomerSelected) selectedTextColor else defaultTextColor)
        }

        val isOwnerSelected = (selectedUserType == UserType.OWNER)
        binding.cardViewOwner.apply {
            isChecked = isOwnerSelected
            strokeColor = if (isOwnerSelected) selectedStrokeColor else defaultStrokeColor
            setCardBackgroundColor(if (isOwnerSelected) selectedBackgroundColor else defaultBackgroundColor)
            binding.textViewOwner.setTextColor(if (isOwnerSelected) selectedTextColor else defaultTextColor)
        }
    }

    private fun updateMemberRoleOnServer(role: UserType) {
        val memberApi = RetrofitClient.getMemberApi(requireContext())
        val request = MemberRoleRequest(role = role.name) // "CUSTOMER" 또는 "OWNER"

        memberApi.updateMemberRole(request).enqueue(object : Callback<MemberRoleResponse> {
            override fun onResponse(call: Call<MemberRoleResponse>, response: Response<MemberRoleResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val responseData = response.body()?.data

                    // 1. 역할 업데이트 성공 후 토큰 재발급 절차 진행
                    if (responseData != null) {
                        performTokenReissue(responseData.refreshToken, role)
                    }
                } else {
                    showErrorDialog(response.body()?.message ?: "역할 설정에 실패했습니다.")
                }
            }

            override fun onFailure(call: Call<MemberRoleResponse>, t: Throwable) {
                showErrorDialog("네트워크 연결 상태를 확인해주세요.")
            }
        })
    }

    private fun performTokenReissue(refreshToken: String, role: UserType) {
        val authApi = RetrofitClient.getAuthApi(requireContext())
        val authProvider = TokenManager.getAuthProvider(requireContext()) ?: "LOCAL"
        val cookieHeader = "refreshToken=$refreshToken"

        val request = ReissueRequest(refreshToken, authProvider)

        authApi.reissue(cookieHeader, authProvider).enqueue(object : Callback<ReissueResponse> {
            override fun onResponse(call: Call<ReissueResponse>, response: Response<ReissueResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val newData = response.body()?.data
                    if (newData != null) {
                        // 새 토큰 저장
                        TokenManager.saveAccessToken(requireContext(), newData.accessToken)
                        TokenManager.saveRefreshToken(requireContext(), newData.refreshToken)
                        TokenManager.saveUserRole(requireContext(), role.name)

                        handleNavigationAfterRoleUpdate(role)
                    }
                } else {
                    // 실패 시 로그 확인용 (404 등이 여기서 발생할 것임)
                    showErrorDialog("토큰 재발급 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ReissueResponse>, t: Throwable) {
                showErrorDialog("재발급 중 오류가 발생했습니다.")
            }
        })
    }

    private fun handleNavigationAfterRoleUpdate(role: UserType) {
        if (role == UserType.OWNER) {
            findNavController().navigate(R.id.action_userTypeFragment_to_ownerOnboardingFragment)
        } else {
            val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                putExtra("USER_TYPE", role.name)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun showErrorDialog(message: String) {
        CommonInfoDialog(
            message = message,
            onConfirm = { }
        ).show(parentFragmentManager, "RoleUpdateError")
    }

    override fun onResume() {
        super.onResume()
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}