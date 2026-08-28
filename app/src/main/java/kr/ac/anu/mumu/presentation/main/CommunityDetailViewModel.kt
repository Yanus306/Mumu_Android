package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.model.CommunityComment
import kr.ac.anu.mumu.domain.model.CommunityPost
import kr.ac.anu.mumu.domain.repository.CommunityRepository
import javax.inject.Inject

sealed interface CommunityDetailUiState {
    data object Loading : CommunityDetailUiState
    data class Success(
        val post: CommunityPost,
        val comments: List<CommunityComment>,
        val currentUserId: Long? = null,
        val liked: Boolean = false,
        val bookmarked: Boolean = false,
        val isProcessing: Boolean = false
    ) : CommunityDetailUiState {
        val isPostOwner: Boolean get() = currentUserId != null && currentUserId == post.userId
    }
    data class Error(val message: String) : CommunityDetailUiState
}

sealed interface CommunityDetailEvent {
    data object PostDeleted : CommunityDetailEvent
    data class Message(val text: String) : CommunityDetailEvent
    data object CommentSaved : CommunityDetailEvent
}

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CommunityRepository
) : ViewModel() {

    val postId: Long = checkNotNull(savedStateHandle["postId"])
    private val _uiState = MutableStateFlow<CommunityDetailUiState>(CommunityDetailUiState.Loading)
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<CommunityDetailEvent>()
    val events: SharedFlow<CommunityDetailEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CommunityDetailUiState.Loading
            repository.getPost(postId)
                .onSuccess { post ->
                    val comments = repository.getComments(postId).getOrDefault(emptyList())
                    val currentUserId = repository.getCurrentUserId().getOrNull()
                    _uiState.value = CommunityDetailUiState.Success(post, comments, currentUserId)
                }
                .onFailure { error ->
                    _uiState.value = CommunityDetailUiState.Error(
                        error.message ?: "게시글을 불러오지 못했습니다."
                    )
                }
        }
    }

    fun toggleLike() = mutate(
        action = { repository.toggleLike(postId) },
        update = { current, (liked, count) ->
            current.copy(post = current.post.copy(likeCount = count), liked = liked)
        },
        fallbackMessage = "좋아요 처리에 실패했습니다."
    )

    fun toggleBookmark() = mutate(
        action = { repository.toggleBookmark(postId) },
        update = { current, (bookmarked, count) ->
            current.copy(post = current.post.copy(bookmarkCount = count), bookmarked = bookmarked)
        },
        fallbackMessage = "북마크 처리에 실패했습니다."
    )

    fun addComment(content: String) {
        if (content.isBlank()) return
        mutate(
            action = { repository.createComment(postId, content.trim()) },
            update = { current, comment ->
                current.copy(
                    post = current.post.copy(commentCount = current.post.commentCount + 1),
                    comments = current.comments + comment
                )
            },
            fallbackMessage = "댓글을 등록하지 못했습니다.",
            successEvent = CommunityDetailEvent.CommentSaved
        )
    }

    fun updateComment(commentId: Long, content: String) {
        if (content.isBlank()) return
        mutate(
            action = { repository.updateComment(postId, commentId, content.trim()) },
            update = { current, updated ->
                current.copy(comments = current.comments.map { if (it.id == commentId) updated else it })
            },
            fallbackMessage = "댓글을 수정하지 못했습니다.",
            successEvent = CommunityDetailEvent.CommentSaved
        )
    }

    fun deleteComment(commentId: Long) {
        mutate(
            action = { repository.deleteComment(postId, commentId) },
            update = { current, _ ->
                current.copy(
                    post = current.post.copy(
                        commentCount = (current.post.commentCount - 1).coerceAtLeast(0)
                    ),
                    comments = current.comments.filterNot { it.id == commentId }
                )
            },
            fallbackMessage = "댓글을 삭제하지 못했습니다."
        )
    }

    fun deletePost() {
        val current = _uiState.value as? CommunityDetailUiState.Success ?: return
        if (current.isProcessing || !current.isPostOwner) return
        viewModelScope.launch {
            _uiState.value = current.copy(isProcessing = true)
            repository.deletePost(postId)
                .onSuccess { _events.emit(CommunityDetailEvent.PostDeleted) }
                .onFailure { error ->
                    _uiState.value = current
                    _events.emit(
                        CommunityDetailEvent.Message(error.message ?: "게시글을 삭제하지 못했습니다.")
                    )
                }
        }
    }

    private fun <T> mutate(
        action: suspend () -> Result<T>,
        update: (CommunityDetailUiState.Success, T) -> CommunityDetailUiState.Success,
        fallbackMessage: String,
        successEvent: CommunityDetailEvent? = null
    ) {
        val current = _uiState.value as? CommunityDetailUiState.Success ?: return
        if (current.isProcessing) return
        viewModelScope.launch {
            _uiState.value = current.copy(isProcessing = true)
            action()
                .onSuccess { result ->
                    _uiState.value = update(current, result).copy(isProcessing = false)
                    successEvent?.let { _events.emit(it) }
                }
                .onFailure { error ->
                    _uiState.value = current
                    _events.emit(CommunityDetailEvent.Message(error.message ?: fallbackMessage))
                }
        }
    }
}
