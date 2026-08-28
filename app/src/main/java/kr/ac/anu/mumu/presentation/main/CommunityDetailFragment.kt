package kr.ac.anu.mumu.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil3.load
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.R
import kr.ac.anu.mumu.databinding.FragmentCommunityDetailBinding
import kr.ac.anu.mumu.domain.model.CommunityComment
import kr.ac.anu.mumu.presentation.main.adapter.CommunityCommentAdapter

@AndroidEntryPoint
class CommunityDetailFragment : Fragment() {

    private var _binding: FragmentCommunityDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityDetailViewModel by viewModels()
    private val commentAdapter = CommunityCommentAdapter(
        onEdit = ::startEditingComment,
        onDelete = ::confirmDeleteComment
    )
    private var editingCommentId: Long? = null
    private var hasResumed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editingCommentId = savedInstanceState?.getLong(KEY_EDITING_COMMENT_ID)?.takeIf { it > 0L }
    }

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
        binding.btnEditPost.setOnClickListener {
            findNavController().navigate(
                R.id.action_communityDetailFragment_to_communityWriteFragment,
                Bundle().apply { putLong("postId", viewModel.postId) }
            )
        }
        binding.btnDeletePost.setOnClickListener { confirmDeletePost() }
        binding.btnCommentCancel.setOnClickListener { clearCommentEditor() }
        if (editingCommentId != null) {
            binding.btnComment.text = "수정"
            binding.btnCommentCancel.visibility = View.VISIBLE
        }
        binding.btnComment.setOnClickListener {
            val content = binding.etComment.text?.toString().orEmpty()
            if (content.isNotBlank()) {
                editingCommentId?.let { commentId ->
                    viewModel.updateComment(commentId, content)
                } ?: viewModel.addComment(content)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasResumed) viewModel.load() else hasResumed = true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        editingCommentId?.let { outState.putLong(KEY_EDITING_COMMENT_ID, it) }
        super.onSaveInstanceState(outState)
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
                binding.tvPostAuthor.text = "사용자 ${post.userId}"
                binding.tvCategory.text = post.category.toCategoryLabel()
                binding.tvTitle.text = post.title
                binding.tvContent.text = post.content
                binding.tvMeta.text =
                    "${post.createdAt.take(10).replace('-', '.')} · 조회 ${post.viewCount} · 댓글 ${post.commentCount}"
                binding.tvHashtags.text = post.hashtags.joinToString(" ") { "#$it" }
                binding.tvHashtags.visibility = if (post.hashtags.isEmpty()) View.GONE else View.VISIBLE
                binding.btnLike.text = if (state.liked) "♥ ${post.likeCount}" else "♡ ${post.likeCount}"
                binding.btnBookmark.text = if (state.bookmarked) "북마크됨" else "북마크 ${post.bookmarkCount}"
                binding.layoutPostManage.visibility = if (state.isPostOwner) View.VISIBLE else View.GONE
                binding.btnEditPost.isEnabled = !state.isProcessing
                binding.btnDeletePost.isEnabled = !state.isProcessing
                binding.btnLike.isEnabled = !state.isProcessing
                binding.btnBookmark.isEnabled = !state.isProcessing
                binding.btnComment.isEnabled = !state.isProcessing
                binding.btnCommentCancel.isEnabled = !state.isProcessing
                if (post.thumbnailUrl.isNullOrBlank()) {
                    binding.ivThumbnail.visibility = View.GONE
                } else {
                    binding.ivThumbnail.visibility = View.VISIBLE
                    binding.ivThumbnail.load(post.thumbnailUrl)
                }
                commentAdapter.setCurrentUserId(state.currentUserId)
                commentAdapter.submitList(state.comments)
                binding.tvCommentEmpty.visibility = if (state.comments.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun startEditingComment(comment: CommunityComment) {
        editingCommentId = comment.id
        binding.etComment.setText(comment.content)
        binding.etComment.setSelection(comment.content.length)
        binding.btnComment.text = "수정"
        binding.btnCommentCancel.visibility = View.VISIBLE
        binding.etComment.requestFocus()
    }

    private fun clearCommentEditor() {
        editingCommentId = null
        binding.etComment.text?.clear()
        binding.btnComment.text = "등록"
        binding.btnCommentCancel.visibility = View.GONE
    }

    private fun confirmDeletePost() {
        AlertDialog.Builder(requireContext())
            .setTitle("게시글을 삭제할까요?")
            .setMessage("삭제한 게시글은 다시 복구할 수 없어요.")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ -> viewModel.deletePost() }
            .show()
    }

    private fun confirmDeleteComment(comment: CommunityComment) {
        AlertDialog.Builder(requireContext())
            .setTitle("댓글을 삭제할까요?")
            .setMessage("삭제한 댓글은 다시 복구할 수 없어요.")
            .setNegativeButton("취소", null)
            .setPositiveButton("삭제") { _, _ ->
                if (editingCommentId == comment.id) clearCommentEditor()
                viewModel.deleteComment(comment.id)
            }
            .show()
    }

    private fun handleEvent(event: CommunityDetailEvent) {
        when (event) {
            CommunityDetailEvent.PostDeleted -> {
                Toast.makeText(requireContext(), "게시글을 삭제했습니다.", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            CommunityDetailEvent.CommentSaved -> clearCommentEditor()
            is CommunityDetailEvent.Message -> {
                Toast.makeText(requireContext(), event.text, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        const val KEY_EDITING_COMMENT_ID = "editing_comment_id"
    }
}

private fun String.toCategoryLabel(): String = when (uppercase()) {
    "FREE" -> "자유"
    "QUESTION" -> "질문"
    "INFO" -> "정보"
    "BRAG" -> "자랑"
    "REVIEW" -> "후기"
    else -> this
}
