package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
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
import kr.ac.anu.mumu.databinding.FragmentCommunityBinding
import kr.ac.anu.mumu.presentation.main.adapter.CommunityPopularPostAdapter

@AndroidEntryPoint
class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private val popularPostAdapter = CommunityPopularPostAdapter { post ->
        findNavController().navigate(
            R.id.communityDetailFragment,
            Bundle().apply { putLong("postId", post.id) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPopularPosts.apply {
            adapter = popularPostAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
        binding.btnMorePosts.setOnClickListener { navigateToPostList() }
        binding.btnMorePets.setOnClickListener {
            Toast.makeText(requireContext(), "인기 아이 기능은 준비중이에요.", Toast.LENGTH_SHORT).show()
        }
        binding.etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                navigateToPostList(binding.etSearch.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: PostUiState) {
        binding.progressPopularPosts.visibility = if (state is PostUiState.Loading) View.VISIBLE else View.GONE
        when (state) {
            PostUiState.Loading -> popularPostAdapter.submitList(emptyList())
            is PostUiState.Success -> {
                popularPostAdapter.submitList(
                    state.posts.sortedByDescending { it.likeCount }.take(9)
                )
            }
            is PostUiState.Error -> popularPostAdapter.submitList(emptyList())
        }
    }

    private fun navigateToPostList(query: String = "") {
        findNavController().navigate(
            R.id.communityPostListFragment,
            Bundle().apply { putString("query", query.trim()) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
