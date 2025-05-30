package com.pethealthmonitor.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealthmonitor.R
import com.pethealthmonitor.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(private val listener: OnNotificationClickListener) :
    ListAdapter<Notification, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    interface OnNotificationClickListener {
        fun onNotificationClick(notification: Notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(getItem(position))
                }
            }
        }

        fun bind(notification: Notification) {
            binding.alertTitleTextView.text = notification.title
            binding.petNameTextView.text = notification.message

            // Format timestamp
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = Date(notification.timestamp)
            binding.timestampTextView.text = dateFormat.format(date)

            // Set icon and color based on notification type
            when (notification.type) {
                NotificationType.TEMPERATURE -> {
                    binding.alertIconImageView.setImageResource(R.drawable.ic_temperature)
                    binding.alertIconImageView.setColorFilter(
                        ContextCompat.getColor(binding.root.context, R.color.red)
                    )
                }
                NotificationType.HEART_RATE -> {
                    binding.alertIconImageView.setImageResource(R.drawable.ic_heart)
                    binding.alertIconImageView.setColorFilter(
                        ContextCompat.getColor(binding.root.context, R.color.red)
                    )
                }
                NotificationType.ACTIVITY_LEVEL -> {
                    binding.alertIconImageView.setImageResource(R.drawable.ic_activity)
                    binding.alertIconImageView.setColorFilter(
                        ContextCompat.getColor(binding.root.context, R.color.blue)
                    )
                }
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Notification, newItem: Notification): Boolean {
            return oldItem == newItem
        }
    }
}