package com.pethealthmonitor.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pethealthmonitor.R
import com.pethealthmonitor.data.model.Pet
import com.pethealthmonitor.databinding.FragmentPetsBinding
import com.pethealthmonitor.util.PreferenceHelper
import com.pethealthmonitor.util.showToast

class PetsFragment : Fragment(), PetsAdapter.OnPetClickListener {

    private var _binding: FragmentPetsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PetsViewModel by viewModels()
    private lateinit var petsAdapter: PetsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()
        loadPets()
    }

    private fun setupRecyclerView() {
        petsAdapter = PetsAdapter(this)
        binding.petsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = petsAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            loadPets()
        }
    }

    private fun observeViewModel() {
        viewModel.petsResult.observe(viewLifecycleOwner) { result ->
            binding.swipeRefreshLayout.isRefreshing = false

            when (result) {
                is PetsViewModel.PetsResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.emptyTextView.visibility = View.GONE
                }
                is PetsViewModel.PetsResult.Success -> {
                    binding.progressBar.visibility = View.GONE

                    if (result.pets.isEmpty()) {
                        binding.emptyTextView.visibility = View.VISIBLE
                    } else {
                        binding.emptyTextView.visibility = View.GONE
                        petsAdapter.submitList(result.pets)
                    }
                }
                is PetsViewModel.PetsResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyTextView.visibility = View.VISIBLE
                    showToast(result.message)
                }
            }
        }
    }

    private fun loadPets() {
        PreferenceHelper.getAuthToken(requireContext())?.let { token ->
            viewModel.getOwnerPets(token, requireContext())
        }
    }

    override fun onPetClick(pet: Pet) {
        val bundle = android.os.Bundle().apply {
            putString("petId", pet._id)
            putString("petName", pet.name)
        }
        findNavController().navigate(R.id.petDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}