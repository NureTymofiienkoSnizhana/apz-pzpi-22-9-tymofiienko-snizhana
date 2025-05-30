package com.pethealthmonitor.ui.pets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pethealthmonitor.R
import com.pethealthmonitor.data.model.Pet
import com.pethealthmonitor.databinding.ItemPetBinding

class PetsAdapter(private val listener: OnPetClickListener) :
    ListAdapter<Pet, PetsAdapter.PetViewHolder>(PetDiffCallback()) {

    interface OnPetClickListener {
        fun onPetClick(pet: Pet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ItemPetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PetViewHolder(private val binding: ItemPetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onPetClick(getItem(position))
                }
            }
        }

        fun bind(pet: Pet) {
            binding.petNameTextView.text = pet.name ?: "Unknown Pet"
            binding.petTypeBreedTextView.text =
                "${pet.type ?: "Unknown"} - ${pet.breed ?: "Unknown"}"
            binding.petAgeTextView.text =
                "Age: ${pet.age ?: 0} ${if ((pet.age ?: 0) == 1) "year" else "years"}"

            // Set monitoring status
            if (pet.device_id.isNullOrEmpty()) {
                binding.monitoringStatusChip.text = "No Monitoring"
                binding.monitoringStatusChip.chipBackgroundColor =
                    ContextCompat.getColorStateList(binding.root.context, R.color.gray)
            } else {
                binding.monitoringStatusChip.text = "Monitoring Active"
                binding.monitoringStatusChip.chipBackgroundColor =
                    ContextCompat.getColorStateList(binding.root.context, R.color.green)
            }

            // Load pet image
            if (!pet.photo_url.isNullOrEmpty()) {
                Glide.with(binding.root.context)
                    .load(pet.photo_url)
                    .placeholder(R.drawable.ic_pet_placeholder)
                    .error(R.drawable.ic_pet_placeholder)
                    .into(binding.petImageView)
            } else {
                binding.petImageView.setImageResource(R.drawable.ic_pet_placeholder)
            }
        }
    }

    class PetDiffCallback : DiffUtil.ItemCallback<Pet>() {
        override fun areItemsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Pet, newItem: Pet): Boolean {
            return oldItem == newItem
        }
    }
}