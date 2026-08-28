package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
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
import kr.ac.anu.mumu.databinding.FragmentCommunityPostListBinding
import kr.ac.anu.mumu.domain.model.CommunityPost
import kr.ac.anu.mumu.presentation.main.adapter.CommunityFeedAdapter

@AndroidEntryPoint
class CommunityPostListFragment : Fragment() {

    private var _binding: FragmentCommunityPostListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PostViewModel by viewModels()
    private var allPosts: List<CommunityPost> = emptyList()
    private val postAdapter = CommunityFeedAdapter { post ->
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
        _binding = FragmentCommunityPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvPosts.apply {
            adapter = postAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.btnRetry.setOnClickListener { viewModel.loadPosts() }
        binding.etSearch.doAfterTextChanged { filterPosts(it?.toString().orEmpty()) }
        binding.etSearch.setText(arguments?.getString("query").orEmpty())

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
                binding.layoutEmpty.visibility = View.GONE
            }
            is PostUiState.Success -> {
                allPosts = state.posts
                binding.rvPosts.visibility = View.VISIBLE
                filterPosts(binding.etSearch.text?.toString().orEmpty())
            }
            is PostUiState.Error -> {
                binding.rvPosts.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
                binding.tvError.text = state.message
            }
        }
    }

    private fun filterPosts(query: String) {
        val keyword = query.trim()
        val filtered = if (keyword.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                post.title.contains(keyword, ignoreCase = true) ||
                    post.content.contains(keyword, ignoreCase = true) ||
                    post.hashtags.any { it.contains(keyword.removePrefix("#"), ignoreCase = true) }
            }
        }
        postAdapter.submitList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.tvEmpty.text = if (allPosts.isEmpty()) {
            "아직 등록된 게시글이 없어요.\n첫 이야기를 남겨보세요!"
        } else {
            "검색 결과가 없어요.\n다른 단어로 찾아보세요."
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
