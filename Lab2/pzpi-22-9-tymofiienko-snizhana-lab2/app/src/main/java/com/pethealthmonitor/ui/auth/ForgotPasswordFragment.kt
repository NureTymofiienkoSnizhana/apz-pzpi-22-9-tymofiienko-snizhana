package com.pethealthmonitor.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pethealthmonitor.databinding.FragmentForgotPasswordBinding
import com.pethealthmonitor.util.hideKeyboard
import com.pethealthmonitor.util.showToast

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.resetPasswordButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()

            if (validateEmail(email)) {
                it.hideKeyboard()
                viewModel.forgotPassword(email)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.forgotPasswordResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.ForgotPasswordResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.resetPasswordButton.isEnabled = false
                }
                is AuthViewModel.ForgotPasswordResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                    showToast(result.message)

                    // Go back to login screen
                    findNavController().navigateUp()
                }
                is AuthViewModel.ForgotPasswordResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.resetPasswordButton.isEnabled = true
                    showToast(result.message)
                }
            }
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            return false
        }
        binding.emailInputLayout.error = null
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}