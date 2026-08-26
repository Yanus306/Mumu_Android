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
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.databinding.FragmentPostBinding
import kr.ac.anu.mumu.presentation.main.adapter.PostAdapter
import kr.ac.anu.mumu.presentation.main.adapter.PostGridSpacingDecoration

@AndroidEntryPoint
class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private val postAdapter = PostAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(PostGridSpacingDecoration())
        }
        binding.btnRetry.setOnClickListener { viewModel.loadPosts() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: PostUiState) {
        binding.progressPosts.visibility = if (state is PostUiState.Loading) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (state is PostUiState.Error) View.VISIBLE else View.GONE

        when (state) {
            PostUiState.Loading -> {
                binding.rvPosts.visibility = View.GONE
                binding.tvEmpty.visibility = View.GONE
            }
            is PostUiState.Success -> {
                postAdapter.submitList(state.posts)
                binding.rvPosts.visibility = if (state.posts.isEmpty()) View.GONE else View.VISIBLE
                binding.tvEmpty.visibility = if (state.posts.isEmpty()) View.VISIBLE else View.GONE
            }
            is PostUiState.Error -> {
                binding.rvPosts.visibility = View.GONE
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
