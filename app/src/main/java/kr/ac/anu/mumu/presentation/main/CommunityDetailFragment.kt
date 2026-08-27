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
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.databinding.FragmentCommunityDetailBinding
import kr.ac.anu.mumu.presentation.main.adapter.CommunityCommentAdapter

@AndroidEntryPoint
class CommunityDetailFragment : Fragment() {

    private var _binding: FragmentCommunityDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityDetailViewModel by viewModels()
    private val commentAdapter = CommunityCommentAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvComments.apply {
            adapter = commentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
        binding.btnRetry.setOnClickListener { viewModel.load() }
        binding.btnLike.setOnClickListener { viewModel.toggleLike() }
        binding.btnBookmark.setOnClickListener { viewModel.toggleBookmark() }
        binding.btnComment.setOnClickListener {
            val content = binding.etComment.text?.toString().orEmpty()
            if (content.isNotBlank()) {
                viewModel.addComment(content)
                binding.etComment.text?.clear()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: CommunityDetailUiState) {
        binding.progressDetail.visibility = if (state is CommunityDetailUiState.Loading) View.VISIBLE else View.GONE
        binding.layoutError.visibility = if (state is CommunityDetailUiState.Error) View.VISIBLE else View.GONE
        binding.scrollContent.visibility = if (state is CommunityDetailUiState.Success) View.VISIBLE else View.GONE

        when (state) {
            CommunityDetailUiState.Loading -> Unit
            is CommunityDetailUiState.Error -> binding.tvError.text = state.message
            is CommunityDetailUiState.Success -> {
                val post = state.post
                binding.tvCategory.text = post.category
                binding.tvTitle.text = post.title
                binding.tvContent.text = post.content
                binding.tvMeta.text = "조회 ${post.viewCount} · 댓글 ${post.commentCount} · ${post.createdAt.take(10)}"
                binding.tvHashtags.text = post.hashtags.joinToString(" ") { "#$it" }
                binding.tvHashtags.visibility = if (post.hashtags.isEmpty()) View.GONE else View.VISIBLE
                binding.btnLike.text = if (state.liked) "♥ ${post.likeCount}" else "♡ ${post.likeCount}"
                binding.btnBookmark.text = if (state.bookmarked) "북마크됨" else "북마크 ${post.bookmarkCount}"
                if (post.thumbnailUrl.isNullOrBlank()) {
                    binding.ivThumbnail.visibility = View.GONE
                } else {
                    binding.ivThumbnail.visibility = View.VISIBLE
                    binding.ivThumbnail.load(post.thumbnailUrl)
                }
                commentAdapter.submitList(state.comments)
                binding.tvCommentEmpty.visibility = if (state.comments.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
