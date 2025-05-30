// ui/petdetail/HealthHistoryAdapter.kt (или ui/pets/HealthHistoryAdapter.kt если нет отдельной папки petdetail)
package com.pethealthmonitor.ui.petdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealthmonitor.R
import com.pethealthmonitor.data.model.HealthData
import com.pethealthmonitor.databinding.ItemHealthHistoryBinding
import java.text.SimpleDateFormat
import java.util.*

class HealthHistoryAdapter : ListAdapter<HealthData, HealthHistoryAdapter.HealthHistoryViewHolder>(HealthHistoryDiffCallback()) {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthHistoryViewHolder {
        val binding = ItemHealthHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HealthHistoryViewHolder(private val binding: ItemHealthHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HealthData) {
            // Форматируем дату (timestamp в секундах, конвертируем в миллисекунды)
            item.time?.t?.let { timestamp ->
                val date = Date(timestamp * 1000L)
                binding.dateTextView.text = dateFormatter.format(date)
            } ?: run {
                binding.dateTextView.text = "Unknown time"
            }

            // Температура
            binding.temperatureValueTextView.text = "${String.format("%.1f", item.temperature)}°C"
            binding.temperatureValueTextView.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    getTemperatureColor(item.temperature)
                )
            )

            // Сон
            binding.sleepValueTextView.text = "${String.format("%.1f", item.sleep_hours)}h"
            binding.sleepValueTextView.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    getSleepColor(item.sleep_hours)
                )
            )

            // Активность
            binding.activityValueTextView.text = "${String.format("%.0f", item.activity)}%"
            binding.activityValueTextView.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    getActivityColor(item.activity)
                )
            )
        }

        private fun getTemperatureColor(temperature: Double): Int {
            return when {
                temperature >= 37.5 && temperature <= 39.5 -> R.color.green
                temperature < 36.5 || temperature > 40.5 -> R.color.red
                else -> android.R.color.holo_orange_dark
            }
        }

        private fun getSleepColor(sleep: Double): Int {
            return when {
                sleep >= 8.0 && sleep <= 16.0 -> R.color.green
                sleep < 6.0 || sleep > 20.0 -> R.color.red
                else -> android.R.color.holo_orange_dark
            }
        }

        private fun getActivityColor(activity: Double): Int {
            return when {
                activity >= 30.0 && activity <= 80.0 -> R.color.green
                activity < 10.0 || activity > 95.0 -> R.color.red
                else -> android.R.color.holo_orange_dark
            }
        }
    }

    class HealthHistoryDiffCallback : DiffUtil.ItemCallback<HealthData>() {
        override fun areItemsTheSame(oldItem: HealthData, newItem: HealthData): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: HealthData, newItem: HealthData): Boolean {
            return oldItem == newItem
        }
    }
}