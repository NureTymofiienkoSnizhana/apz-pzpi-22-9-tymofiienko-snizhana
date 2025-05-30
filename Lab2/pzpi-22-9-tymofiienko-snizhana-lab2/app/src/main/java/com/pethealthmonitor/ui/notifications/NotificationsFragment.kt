package com.pethealthmonitor.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.pethealthmonitor.R
import com.pethealthmonitor.databinding.FragmentNotificationsBinding
import com.pethealthmonitor.util.PreferenceHelper
import com.pethealthmonitor.util.showToast

class NotificationsFragment : Fragment(), NotificationAdapter.OnNotificationClickListener {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupObservers()
        loadNotifications()
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(this)
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout?.setOnRefreshListener {
            loadNotifications()
        }
    }

    private fun setupObservers() {
        viewModel.notificationsResult.observe(viewLifecycleOwner) { result ->
            binding.swipeRefreshLayout?.isRefreshing = false

            when (result) {
                is NotificationsViewModel.NotificationsResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyTextView.visibility = View.GONE
                }
                is NotificationsViewModel.NotificationsResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    displayNotifications(result.notifications)
                }
                is NotificationsViewModel.NotificationsResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyTextView.visibility = View.VISIBLE
                    showToast(result.message)
                }
            }
        }

        viewModel.healthSummaryResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NotificationsViewModel.HealthSummaryResult.Success -> {
                    updateHealthSummaryInfo(result.healthSummary)
                }
                is NotificationsViewModel.HealthSummaryResult.Error -> {
                    // Обрабатываем ошибки если нужно
                }
            }
        }
    }

    private fun displayNotifications(notifications: List<Notification>) {
        if (notifications.isEmpty()) {
            binding.emptyTextView.visibility = View.VISIBLE
            binding.notificationsRecyclerView.visibility = View.GONE
            binding.emptyTextView.text = "No health alerts at this time"
        } else {
            binding.emptyTextView.visibility = View.GONE
            binding.notificationsRecyclerView.visibility = View.VISIBLE
            notificationAdapter.submitList(notifications)
        }
    }

    private fun updateHealthSummaryInfo(healthSummary: com.pethealthmonitor.data.model.OwnerHealthSummary) {
        val problemsCount = healthSummary.problemsCount ?: 0
        val totalPets = healthSummary.totalPets ?: 0
        val healthyPets = healthSummary.healthyPets ?: 0

        val infoText = when {
            problemsCount == 0 -> "All your pets are healthy! 🎉"
            problemsCount == 1 -> "1 pet needs attention"
            else -> "$problemsCount pets need attention"
        }

        binding.infoTextView.text = "$infoText\n($healthyPets/$totalPets pets are healthy)"
    }

    private fun loadNotifications() {
        PreferenceHelper.getAuthToken(requireContext())?.let { token ->
            viewModel.loadNotifications(token, requireContext())
        } ?: run {
            showToast("Authentication required")
        }
    }

    override fun onNotificationClick(notification: Notification) {
        // Navigate to pet detail screen when notification is clicked
        val bundle = Bundle().apply {
            putString("petId", notification.petId)
            putString("petName", notification.petName)
        }
        findNavController().navigate(R.id.petDetailFragment, bundle)
    }

    override fun onResume() {
        super.onResume()
        // Обновляем данные при возврате на экран
        loadNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}