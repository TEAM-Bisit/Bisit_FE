package com.example.bisit.ui.signUp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.bisit.MainActivity
import com.example.bisit.R
import com.example.bisit.databinding.FragmentUserTypeBinding

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
            val userTypeString = selectedUserType!!.name

            val intent = Intent(requireActivity(), MainActivity::class.java).apply {
                putExtra("USER_TYPE", userTypeString)
            }

            startActivity(intent)

            requireActivity().finish()
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