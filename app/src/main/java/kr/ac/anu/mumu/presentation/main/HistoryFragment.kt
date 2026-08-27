package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentHistoryBinding
import kr.ac.anu.mumu.presentation.main.adapter.HistoryAdapter

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var historyAdapter: HistoryAdapter
    private val viewModel: HistoryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeHistory()
        binding.btnRetry.setOnClickListener { viewModel.loadHistory() }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter { item ->
            findNavController().navigate(
                R.id.analysisHistoryDetailFragment,
                Bundle().apply { putLong("analysisId", item.id) }
            )
        }
        binding.rvHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: HistoryUiState) {
        binding.progressHistory.visibility = if (state is HistoryUiState.Loading) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (state is HistoryUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            HistoryUiState.Loading -> {
                binding.rvHistory.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
            }
            is HistoryUiState.Success -> {
                historyAdapter.submitList(state.items)
                binding.rvHistory.visibility = if (state.items.isEmpty()) View.GONE else View.VISIBLE
                binding.tvEmpty.visibility = if (state.items.isEmpty()) View.VISIBLE else View.GONE
            }
            is HistoryUiState.Error -> {
                binding.rvHistory.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
                binding.tvError.text = state.message
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
