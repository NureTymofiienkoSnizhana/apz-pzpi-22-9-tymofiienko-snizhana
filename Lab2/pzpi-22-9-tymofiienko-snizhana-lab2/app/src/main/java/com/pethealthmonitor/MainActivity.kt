package com.pethealthmonitor.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.pethealthmonitor.R
import com.pethealthmonitor.databinding.ActivityMainBinding
import com.pethealthmonitor.util.HealthMonitorManager
import com.pethealthmonitor.util.PreferenceHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before calling super.onCreate()
        applySavedTheme()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation with the nav controller
        binding.bottomNavigationView.setupWithNavController(navController)

        // Запускаем мониторинг здоровья при старте приложения
        startHealthMonitoringIfLoggedIn()

        // Обрабатываем интент от уведомления
        handleNotificationIntent()
    }

    private fun applySavedTheme() {
        val savedTheme = PreferenceHelper.getCurrentTheme(this)
        val mode = when (savedTheme) {
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun startHealthMonitoringIfLoggedIn() {
        // Проверяем, авторизован ли пользователь
        val token = PreferenceHelper.getAuthToken(this)
        if (!token.isNullOrEmpty()) {
            HealthMonitorManager.startHealthMonitoring(this)
        }
    }

    private fun handleNotificationIntent() {
        if (intent.getBooleanExtra("openPetDetail", false)) {
            val petId = intent.getStringExtra("petId")
            val petName = intent.getStringExtra("petName")

            if (!petId.isNullOrEmpty()) {
                // Навигация к деталям питомца
                val bundle = Bundle().apply {
                    putString("petId", petId)
                    putString("petName", petName)
                }

                // Небольшая задержка чтобы навигация успела инициализироваться
                binding.root.post {
                    try {
                        navController.navigate(R.id.petDetailFragment, bundle)
                    } catch (e: Exception) {
                        // Если навигация не удалась, ничего страшного
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Перезапускаем мониторинг при возврате в приложение
        startHealthMonitoringIfLoggedIn()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}