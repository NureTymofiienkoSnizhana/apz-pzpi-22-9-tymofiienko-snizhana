package com.pethealthmonitor.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pethealthmonitor.databinding.FragmentEditProfileBinding
import com.pethealthmonitor.util.PreferenceHelper
import com.pethealthmonitor.util.showToast

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    private var currentUser: com.pethealthmonitor.data.model.UserResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        setupPasswordToggle()
        observeViewModel()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            if (validateInputs()) {
                saveProfile()
            }
        }

        binding.cancelButton.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.changePhotoButton.setOnClickListener {
            // TODO: Implement photo selection
            showToast("Photo selection - Coming soon!")
        }
    }

    private fun setupPasswordToggle() {
        binding.changePasswordSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.passwordFieldsLayout.visibility = if (isChecked) View.VISIBLE else View.GONE

            if (!isChecked) {
                // Clear password fields when disabled
                binding.currentPasswordEditText.setText("")
                binding.newPasswordEditText.setText("")
                binding.confirmPasswordEditText.setText("")
            }
        }

        // Add text watchers for password validation
        binding.newPasswordEditText.addTextChangedListener {
            validatePasswords()
        }

        binding.confirmPasswordEditText.addTextChangedListener {
            validatePasswords()
        }
    }

    private fun observeViewModel() {
        viewModel.profileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ProfileViewModel.ProfileResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ProfileViewModel.ProfileResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    currentUser = result.user
                    populateFields(result.user)
                }
                is ProfileViewModel.ProfileResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(result.message)
                }
            }
        }

        viewModel.updateProfileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ProfileViewModel.UpdateProfileResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.saveButton.isEnabled = false
                }
                is ProfileViewModel.UpdateProfileResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    showToast("Profile updated successfully!")
                    findNavController().popBackStack()
                }
                is ProfileViewModel.UpdateProfileResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.saveButton.isEnabled = true
                    showToast(result.message)
                }
            }
        }
    }

    private fun populateFields(user: com.pethealthmonitor.data.model.UserResponse) {
        binding.fullNameEditText.setText(user.full_name)
        binding.emailEditText.setText(user.email)
        binding.roleEditText.setText(formatRole(user.role))
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Validate full name
        val fullName = binding.fullNameEditText.text.toString().trim()
        if (fullName.isEmpty()) {
            binding.fullNameInputLayout.error = "Full name is required"
            isValid = false
        } else {
            binding.fullNameInputLayout.error = null
        }

        // Validate email
        val email = binding.emailEditText.text.toString().trim()
        if (email.isEmpty()) {
            binding.emailInputLayout.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Please enter a valid email address"
            isValid = false
        } else {
            binding.emailInputLayout.error = null
        }

        // Validate passwords if password change is enabled
        if (binding.changePasswordSwitch.isChecked) {
            val currentPassword = binding.currentPasswordEditText.text.toString()
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (currentPassword.isEmpty()) {
                binding.currentPasswordInputLayout.error = "Current password is required"
                isValid = false
            } else {
                binding.currentPasswordInputLayout.error = null
            }

            if (newPassword.isEmpty()) {
                binding.newPasswordInputLayout.error = "New password is required"
                isValid = false
            } else if (newPassword.length < 6) {
                binding.newPasswordInputLayout.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                binding.newPasswordInputLayout.error = null
            }

            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordInputLayout.error = "Please confirm your password"
                isValid = false
            } else if (newPassword != confirmPassword) {
                binding.confirmPasswordInputLayout.error = "Passwords do not match"
                isValid = false
            } else {
                binding.confirmPasswordInputLayout.error = null
            }
        }

        return isValid
    }

    private fun validatePasswords() {
        val newPassword = binding.newPasswordEditText.text.toString()
        val confirmPassword = binding.confirmPasswordEditText.text.toString()

        if (newPassword.isNotEmpty() && confirmPassword.isNotEmpty()) {
            if (newPassword != confirmPassword) {
                binding.confirmPasswordInputLayout.error = "Passwords do not match"
            } else {
                binding.confirmPasswordInputLayout.error = null
            }
        }
    }

    private fun saveProfile() {
        val token = PreferenceHelper.getAuthToken(requireContext()) ?: return

        val fullName = binding.fullNameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()

        val password = if (binding.changePasswordSwitch.isChecked) {
            binding.newPasswordEditText.text.toString()
        } else null

        // Check if anything has changed
        val hasChanges = currentUser?.let { user ->
            fullName != user.full_name ||
                    email != user.email ||
                    password != null
        } ?: true

        if (!hasChanges) {
            showToast("No changes to save")
            return
        }

        viewModel.updateUserProfile(
            token = token,
            context = requireContext(),
            fullName = if (fullName != currentUser?.full_name) fullName else null,
            email = if (email != currentUser?.email) email else null,
            password = password
        )
    }

    private fun loadUserProfile() {
        PreferenceHelper.getAuthToken(requireContext())?.let { token ->
            viewModel.getUserProfile(token, requireContext())
        }
    }

    private fun formatRole(role: String): String {
        return when (role) {
            "user" -> "Pet Owner"
            "vet" -> "Veterinarian"
            else -> role.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}