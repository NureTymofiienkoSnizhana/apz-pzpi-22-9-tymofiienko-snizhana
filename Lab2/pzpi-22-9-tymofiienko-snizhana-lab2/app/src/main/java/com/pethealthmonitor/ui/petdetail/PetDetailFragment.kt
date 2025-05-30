package com.pethealthmonitor.ui.petdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.pethealthmonitor.R
import com.pethealthmonitor.data.model.HealthData
import com.pethealthmonitor.data.model.HealthSummary
import com.pethealthmonitor.data.model.PetWithHealth
import com.pethealthmonitor.databinding.FragmentPetDetailBinding
import com.pethealthmonitor.util.PreferenceHelper
import com.pethealthmonitor.util.showToast
import java.text.SimpleDateFormat
import java.util.*

class PetDetailFragment : Fragment() {

    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PetDetailViewModel by viewModels()

    // Добавить адаптеры как свойства класса
    private lateinit var issuesAdapter: IssuesAdapter
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    private lateinit var healthHistoryAdapter: HealthHistoryAdapter

    private var petId: String? = null
    private var petName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем аргументы
        petId = arguments?.getString("petId")
        petName = arguments?.getString("petName")

        setupAdapters()
        setupObservers()
        setupClickListeners()
        loadData()
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupObservers() {
        // Наблюдаем за деталями питомца
        viewModel.petDetailsResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PetDetailViewModel.PetDetailsResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is PetDetailViewModel.PetDetailsResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    displayPetDetails(result.petWithHealth)
                }
                is PetDetailViewModel.PetDetailsResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showToast(result.message)
                }
            }
        }

        viewModel.healthSummaryResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is PetDetailViewModel.HealthSummaryResult.Loading -> {
                    // Показать загрузку если нужно
                }
                is PetDetailViewModel.HealthSummaryResult.Success -> {
                    displayHealthAlert(result.healthSummary)
                }
                is PetDetailViewModel.HealthSummaryResult.Error -> {
                    // Игнорируем ошибки health summary, так как это дополнительная функция
                    // showToast(result.message)
                }
            }
        }
    }

    private fun loadData() {
        petId?.let { id ->
            PreferenceHelper.getAuthToken(requireContext())?.let { token ->
                // Загружаем основные данные питомца (которые включают историю здоровья)
                loadPetDetails(id, token)
                // Дополнительно загружаем summary для алертов (если API доступен)
                loadHealthSummary(id, token)
                // Health History теперь берется из основных данных питомца
            }
        }
    }

    // В onViewCreated добавить инициализацию адаптеров
    private fun setupAdapters() {
        issuesAdapter = IssuesAdapter()
        recommendationsAdapter = RecommendationsAdapter()
        healthHistoryAdapter = HealthHistoryAdapter()

        binding.issuesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = issuesAdapter
        }

        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recommendationsAdapter
        }

        binding.healthHistoryRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = healthHistoryAdapter
        }
    }

    // Метод для отображения алертов
    private fun displayHealthAlert(healthSummary: HealthSummary) {
        val notificationLevel = healthSummary.notification_level
        val issues = healthSummary.issues
        val recommendations = healthSummary.recommendations

        // Если нет проблем и рекомендаций, скрываем карточку алертов
        if (issues.isNullOrEmpty() && recommendations.isNullOrEmpty()) {
            binding.alertCardView.visibility = View.GONE
            return
        }

        // Показываем карточку алертов
        binding.alertCardView.visibility = View.VISIBLE

        // Устанавливаем цвет и текст заголовка в зависимости от уровня
        when (notificationLevel) {
            "urgent" -> {
                binding.alertTitleTextView.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
                binding.alertTitleTextView.text = "Critical Health Alert"
                // Опционально: изменить цвет фона карточки
                binding.alertCardView.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.light_red)
                )
            }
            "warning" -> {
                binding.alertTitleTextView.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.holo_orange_dark)
                )
                binding.alertTitleTextView.text = "Health Warning"
                binding.alertCardView.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.light_orange)
                )
            }
            "info" -> {
                binding.alertTitleTextView.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.green)
                )
                binding.alertTitleTextView.text = "Health Information"
                binding.alertCardView.setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.light_green)
                )
            }
            else -> {
                binding.alertTitleTextView.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.purple_500)
                )
                binding.alertTitleTextView.text = "Health Update"
            }
        }

        // Отображаем проблемы
        if (!issues.isNullOrEmpty()) {
            binding.issuesContainer.visibility = View.VISIBLE
            issuesAdapter.submitList(issues)
        } else {
            binding.issuesContainer.visibility = View.GONE
        }

        // Отображаем рекомендации
        if (!recommendations.isNullOrEmpty()) {
            binding.recommendationsContainer.visibility = View.VISIBLE
            recommendationsAdapter.submitList(recommendations)
        } else {
            binding.recommendationsContainer.visibility = View.GONE
        }
    }

    // Метод для отображения истории здоровья из данных HealthData
    private fun displayHealthHistoryFromHealthData(healthData: List<HealthData>?) {
        if (healthData.isNullOrEmpty()) {
            binding.noHistoryDataTextView.visibility = View.VISIBLE
            binding.healthHistoryRecyclerView.visibility = View.GONE
        } else {
            binding.noHistoryDataTextView.visibility = View.GONE
            binding.healthHistoryRecyclerView.visibility = View.VISIBLE

            // Сортируем по времени (новые записи сначала)
            val sortedData = healthData.sortedByDescending { it.time?.t ?: 0L }
            healthHistoryAdapter.submitList(sortedData)
        }
    }

    // Метод для отображения истории здоровья (если используется отдельный API)
    private fun displayHealthHistory(healthData: List<HealthData>) {
        if (healthData.isEmpty()) {
            binding.noHistoryDataTextView.visibility = View.VISIBLE
            binding.healthHistoryRecyclerView.visibility = View.GONE
        } else {
            binding.noHistoryDataTextView.visibility = View.GONE
            binding.healthHistoryRecyclerView.visibility = View.VISIBLE
            // Сортируем по времени (новые записи сначала)
            val sortedData = healthData.sortedByDescending { it.time?.t ?: 0L }
            healthHistoryAdapter.submitList(sortedData)
        }
    }

    // Методы для загрузки данных
    private fun loadPetDetails(petId: String, token: String) {
        viewModel.getPetDetails(petId, token, requireContext())
    }

    private fun loadHealthSummary(petId: String, token: String) {
        viewModel.getPetHealthSummary(petId, token, requireContext())
    }

    // Метод для отображения деталей питомца
    private fun displayPetDetails(petWithHealth: PetWithHealth) {
        // Отображаем основную информацию о питомце
        binding.petNameTextView.text = petWithHealth.name ?: "Unknown Pet"
        binding.petTypeTextView.text = "Type: ${petWithHealth.species ?: "Unknown"}"
        binding.petBreedTextView.text = "Breed: ${petWithHealth.breed ?: "Unknown"}"
        binding.petAgeTextView.text = "Age: ${petWithHealth.age ?: 0} ${if ((petWithHealth.age ?: 0) == 1) "year" else "years"}"

        // Загружаем изображение питомца
        if (!petWithHealth.photo_url.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(petWithHealth.photo_url)
                .placeholder(R.drawable.ic_pet_placeholder)
                .error(R.drawable.ic_pet_placeholder)
                .into(binding.petImageView)
        } else {
            binding.petImageView.setImageResource(R.drawable.ic_pet_placeholder)
        }

        // Отображаем последние данные о здоровье
        displayCurrentHealthData(petWithHealth.health)

        // Отображаем историю здоровья (используем те же данные, но отсортированные)
        displayHealthHistoryFromHealthData(petWithHealth.health)
    }

    // Метод для отображения текущих данных о здоровье
    private fun displayCurrentHealthData(healthData: List<HealthData>?) {
        if (healthData.isNullOrEmpty()) {
            binding.noHealthDataTextView.visibility = View.VISIBLE
            binding.healthDataContainer.visibility = View.GONE
            return
        }

        // Находим последнюю запись (с максимальным timestamp)
        val latestHealth = healthData.maxByOrNull { it.time?.t ?: 0L }

        if (latestHealth == null) {
            binding.noHealthDataTextView.visibility = View.VISIBLE
            binding.healthDataContainer.visibility = View.GONE
            return
        }

        binding.noHealthDataTextView.visibility = View.GONE
        binding.healthDataContainer.visibility = View.VISIBLE

        // Отображаем данные
        binding.temperatureTextView.text = "${String.format("%.1f", latestHealth.temperature)}°C"
        binding.activityLevelTextView.text = "${String.format("%.0f", latestHealth.activity)}%"

        // Heart Rate пока что не приходит с API, показываем активность
        binding.heartRateTextView.text = "${String.format("%.0f", latestHealth.activity)} units"

        // Форматируем время (timestamp в секундах, нужно перевести в миллисекунды)
        latestHealth.time?.t?.let { timestamp ->
            val date = Date(timestamp * 1000L)
            val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.lastUpdateTextView.text = formatter.format(date)
        }

        // Устанавливаем цвета в зависимости от значений
        setHealthDataColors(latestHealth)
    }

    private fun setHealthDataColors(healthData: HealthData) {
        // Цвет для температуры
        val tempColor = when {
            healthData.temperature >= 37.5 && healthData.temperature <= 39.5 -> R.color.green
            healthData.temperature < 36.5 || healthData.temperature > 40.5 -> R.color.red
            else -> android.R.color.holo_orange_dark
        }
        binding.temperatureTextView.setTextColor(ContextCompat.getColor(requireContext(), tempColor))

        // Цвет для активности
        val activityColor = when {
            healthData.activity >= 30.0 && healthData.activity <= 80.0 -> R.color.green
            healthData.activity < 10.0 || healthData.activity > 95.0 -> R.color.red
            else -> android.R.color.holo_orange_dark
        }
        binding.activityLevelTextView.setTextColor(ContextCompat.getColor(requireContext(), activityColor))
        binding.heartRateTextView.setTextColor(ContextCompat.getColor(requireContext(), activityColor))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}