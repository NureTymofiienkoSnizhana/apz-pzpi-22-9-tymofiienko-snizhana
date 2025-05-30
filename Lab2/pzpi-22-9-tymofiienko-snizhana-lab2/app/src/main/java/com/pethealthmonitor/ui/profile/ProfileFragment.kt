package com.pethealthmonitor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pethealthmonitor.R
import com.pethealthmonitor.databinding.FragmentProfileBinding
import com.pethealthmonitor.ui.auth.AuthActivity
import com.pethealthmonitor.ui.auth.AuthViewModel
import com.pethealthmonitor.util.PreferenceHelper
import com.pethealthmonitor.util.showToast
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
        setupAppSettings()

        // Показываем сохраненные данные сразу, если они есть
        showCachedDataIfAvailable()

        // Затем загружаем актуальные данные с сервера
        loadUserProfile()
    }

    private fun showCachedDataIfAvailable() {
        val savedName = PreferenceHelper.getUserName(requireContext())
        val savedEmail = PreferenceHelper.getUserEmail(requireContext())
        val savedUserId = PreferenceHelper.getUserId(requireContext())
        val savedRole = PreferenceHelper.getUserRole(requireContext())

        if (!savedName.isNullOrEmpty()) {
            binding.nameTextView.text = savedName
        }

        if (!savedEmail.isNullOrEmpty()) {
            binding.emailTextView.text = savedEmail
        }

        if (!savedUserId.isNullOrEmpty()) {
            binding.userIdTextView.text = "ID: ${savedUserId.take(8)}..."
        }

        if (!savedRole.isNullOrEmpty()) {
            binding.roleTextView.text = formatRole(savedRole)
        }
    }

    private fun setupClickListeners() {
        binding.editProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.logoutButton.setOnClickListener {
            PreferenceHelper.getAuthToken(requireContext())?.let { token ->
                authViewModel.logout(token)
            } ?: navigateToLoginScreen()
        }

        // Test: долгое нажатие на аватар для тестирования данных
        binding.profileImageView.setOnLongClickListener {
            showTestData()
            true
        }

        // Settings click listeners
        binding.notificationsSettingLayout.setOnClickListener {
            // Toggle notifications switch
            binding.notificationsSwitch.toggle()
            updateNotificationsPreference(binding.notificationsSwitch.isChecked)
        }

        binding.themeSettingLayout.setOnClickListener {
            // Show theme selection dialog
            showThemeSelectionDialog()
        }

        binding.privacySettingLayout.setOnClickListener {
            // Navigate to privacy settings (implement as needed)
            showToast("Privacy settings - Coming soon!")
        }

        // Notifications switch listener
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationsPreference(isChecked)
        }
    }

    private fun showTestData() {
        binding.nameTextView.text = "John Doe"
        binding.emailTextView.text = "john.doe@example.com"
        binding.userIdTextView.text = "ID: 682c16c1..."
        binding.roleTextView.text = "Pet Owner"
        showToast("Test data loaded")
    }

    private fun setupAppSettings() {
        // Load saved preferences
        val notificationsEnabled = PreferenceHelper.getNotificationsEnabled(requireContext())
        binding.notificationsSwitch.isChecked = notificationsEnabled

        val currentTheme = PreferenceHelper.getCurrentTheme(requireContext())
        binding.currentThemeTextView.text = when (currentTheme) {
            "dark" -> "Dark theme"
            else -> "Light theme"
        }
    }

    private fun observeViewModel() {
        viewModel.profileResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is ProfileViewModel.ProfileResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    // Покажем placeholder данные во время загрузки
                    binding.nameTextView.text = "Loading..."
                    binding.emailTextView.text = "Loading..."
                }
                is ProfileViewModel.ProfileResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    populateUserData(result.user)
                }
                is ProfileViewModel.ProfileResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast("Error: ${result.message}")

                    // Показываем fallback данные из SharedPreferences если есть
                    val savedName = PreferenceHelper.getUserName(requireContext())
                    val savedEmail = PreferenceHelper.getUserEmail(requireContext())

                    if (!savedName.isNullOrEmpty()) {
                        binding.nameTextView.text = savedName
                    } else {
                        binding.nameTextView.text = "Name not available"
                    }

                    if (!savedEmail.isNullOrEmpty()) {
                        binding.emailTextView.text = savedEmail
                    } else {
                        binding.emailTextView.text = "Email not available"
                    }

                    binding.userIdTextView.text = "ID: Not available"
                    binding.roleTextView.text = "Role not available"
                }
            }
        }

        authViewModel.logoutResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is AuthViewModel.LogoutResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.logoutButton.isEnabled = false
                }
                is AuthViewModel.LogoutResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.logoutButton.isEnabled = true

                    // Clear user data and navigate to login screen
                    PreferenceHelper.clearUserData(requireContext())
                    navigateToLoginScreen()
                }
                is AuthViewModel.LogoutResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.logoutButton.isEnabled = true
                    showToast(result.message)

                    // Even if there's an error, we'll clear user data and navigate to login screen
                    PreferenceHelper.clearUserData(requireContext())
                    navigateToLoginScreen()
                }
            }
        }
    }

    private fun populateUserData(user: com.pethealthmonitor.data.model.UserResponse) {
        // Basic user info
        binding.nameTextView.text = user.full_name
        binding.emailTextView.text = user.email
        binding.userIdTextView.text = "ID: ${user._id.take(8)}..." // Show truncated ID

        // Role information
        binding.roleTextView.text = formatRole(user.role)
    }

    private fun loadUserProfile() {
        val token = PreferenceHelper.getAuthToken(requireContext())
        val savedRole = PreferenceHelper.getUserRole(requireContext())

        if (token != null) {
            showToast("Loading profile... Token: ${token.take(10)}..., Role: ${savedRole ?: "Unknown"}")
            viewModel.getUserProfile(token, requireContext())
        } else {
            // If no token, show fallback data or navigate to login
            showToast("No auth token found - please log in again")
            navigateToLoginScreen()
        }
    }

    private fun formatRole(role: String): String {
        return when (role) {
            "user" -> "Pet Owner"
            "vet" -> "Veterinarian"
            "admin" -> "Administrator"
            else -> role.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }
    }

    private fun updateNotificationsPreference(enabled: Boolean) {
        PreferenceHelper.setNotificationsEnabled(requireContext(), enabled)
        showToast(if (enabled) "Notifications enabled" else "Notifications disabled")
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Light theme", "Dark theme")
        val currentTheme = PreferenceHelper.getCurrentTheme(requireContext())
        val selectedIndex = when (currentTheme) {
            "dark" -> 1
            else -> 0
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Choose Theme")
        builder.setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
            val newTheme = when (which) {
                1 -> "dark"
                else -> "light"
            }

            PreferenceHelper.setCurrentTheme(requireContext(), newTheme)
            binding.currentThemeTextView.text = themes[which]

            // Apply theme immediately
            applyTheme(newTheme)

            showToast("Theme updated to ${themes[which]}")
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun applyTheme(theme: String) {
        val mode = when (theme) {
            "dark" -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(requireContext(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}