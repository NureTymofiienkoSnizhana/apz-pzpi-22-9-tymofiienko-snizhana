package com.pethealthmonitor.util

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val PREF_NAME = "pet_health_prefs"

    // Auth keys
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"

    // App settings keys
    private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    private const val KEY_CURRENT_THEME = "current_theme"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    private const val KEY_LAST_SYNC = "last_sync"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // Auth methods
    fun saveAuthToken(context: Context, token: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .apply()
    }

    fun getAuthToken(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_AUTH_TOKEN, null)
    }

    fun saveUserRole(context: Context, role: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getUserRole(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ROLE, null)
    }

    fun saveUserId(context: Context, userId: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_ID, null)
    }

    fun saveUserEmail(context: Context, email: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_EMAIL, null)
    }

    fun saveUserName(context: Context, name: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUserName(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USER_NAME, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getAuthToken(context) != null
    }

    fun clearUserData(context: Context) {
        getSharedPreferences(context).edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_NAME)
            .apply()
    }

    fun getNotificationsEnabled(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setCurrentTheme(context: Context, theme: String) {
        getSharedPreferences(context).edit()
            .putString(KEY_CURRENT_THEME, theme)
            .apply()
    }

    fun getCurrentTheme(context: Context): String {
        return getSharedPreferences(context).getString(KEY_CURRENT_THEME, "light") ?: "light"
    }

    fun setFirstLaunch(context: Context, isFirst: Boolean) {
        getSharedPreferences(context).edit()
            .putBoolean(KEY_FIRST_LAUNCH, isFirst)
            .apply()
    }

    fun isFirstLaunch(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setLastSyncTime(context: Context, timestamp: Long) {
        getSharedPreferences(context).edit()
            .putLong(KEY_LAST_SYNC, timestamp)
            .apply()
    }

    fun getLastSyncTime(context: Context): Long {
        return getSharedPreferences(context).getLong(KEY_LAST_SYNC, 0L)
    }

    // Методы для управления уведомлениями
    fun saveLastNotificationTime(context: Context, petId: String, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong("last_notification_$petId", timestamp).apply()
    }

    fun getLastNotificationTime(context: Context, petId: String): Long {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getLong("last_notification_$petId", 0L)
    }

    // Методы для настроек уведомлений
    fun setNotificationsEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
    }

    fun areNotificationsEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("notifications_enabled", true)
    }

    fun setHealthCheckInterval(context: Context, intervalMinutes: Int) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("health_check_interval", intervalMinutes).apply()
    }

    fun getHealthCheckInterval(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("health_check_interval", 30) // По умолчанию 30 минут
    }

    // Utility methods
    fun saveUserData(
        context: Context,
        token: String,
        role: String,
        userId: String,
        email: String,
        name: String
    ) {
        getSharedPreferences(context).edit()
            .putString(KEY_AUTH_TOKEN, token)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun clearAllData(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }
}