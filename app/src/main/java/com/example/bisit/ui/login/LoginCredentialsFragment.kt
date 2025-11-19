package com.example.bisit.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.bisit.MainActivity // 메인 액티비티 임포트
import com.example.bisit.R
import com.example.bisit.databinding.FragmentLoginCredentialsBinding

class LoginCredentialsFragment : Fragment() {

    private var _binding: FragmentLoginCredentialsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginCredentialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupTextWatchers()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val id = binding.etId.text.toString()
            val pw = binding.etPassword.text.toString()

            // ViewModel에 로그인 요청
            viewModel.login(requireContext(), id, pw)
        }

        binding.tvFindId.setOnClickListener {
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findIdFragment)
        }

        binding.tvFindPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginCredentialsFragment_to_findPasswordFragment)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                Toast.makeText(context, "로그인 성공!", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    private fun setupTextWatchers() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateLoginButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etId.addTextChangedListener(textWatcher)
        binding.etPassword.addTextChangedListener(textWatcher)
    }

    private fun updateLoginButtonState() {
        val isIdValid = binding.etId.text.isNullOrBlank().not()
        val isPasswordValid = binding.etPassword.text.isNullOrBlank().not()
        binding.btnLogin.isEnabled = isIdValid && isPasswordValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}