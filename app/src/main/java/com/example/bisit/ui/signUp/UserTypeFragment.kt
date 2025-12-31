package com.example.bisit.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.data.api.RetrofitClient
import com.example.bisit.data.model.member.MemberRoleResponse
import com.example.bisit.databinding.FragmentUserTypeBinding
import com.example.bisit.data.model.member.MemberRoleRequest
import com.example.bisit.ui.dialog.CommonInfoDialog
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
                    handleNavigationAfterRoleUpdate(role)
                } else {
                    val errorMsg = response.body()?.message ?: "역할 설정에 실패했습니다."
                    showErrorDialog(errorMsg)
                }
            }

            override fun onFailure(call: Call<MemberRoleResponse>, t: Throwable) {
                showErrorDialog("네트워크 연결 상태를 확인해주세요.")
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