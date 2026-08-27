package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
        val liked: Boolean = false,
        val bookmarked: Boolean = false
    ) : CommunityDetailUiState
    data class Error(val message: String) : CommunityDetailUiState
}

@HiltViewModel
class CommunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CommunityRepository
) : ViewModel() {

    private val postId: Long = checkNotNull(savedStateHandle["postId"])
    private val _uiState = MutableStateFlow<CommunityDetailUiState>(CommunityDetailUiState.Loading)
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = CommunityDetailUiState.Loading
            repository.getPost(postId)
                .onSuccess { post ->
                    val comments = repository.getComments(postId).getOrDefault(emptyList())
                    _uiState.value = CommunityDetailUiState.Success(post, comments)
                }
                .onFailure { error ->
                    _uiState.value = CommunityDetailUiState.Error(
                        error.message ?: "게시글을 불러오지 못했습니다."
                    )
                }
        }
    }

    fun toggleLike() {
        val current = _uiState.value as? CommunityDetailUiState.Success ?: return
        viewModelScope.launch {
            repository.toggleLike(postId).onSuccess { (liked, count) ->
                _uiState.value = current.copy(
                    post = current.post.copy(likeCount = count),
                    liked = liked
                )
            }
        }
    }

    fun toggleBookmark() {
        val current = _uiState.value as? CommunityDetailUiState.Success ?: return
        viewModelScope.launch {
            repository.toggleBookmark(postId).onSuccess { (bookmarked, count) ->
                _uiState.value = current.copy(
                    post = current.post.copy(bookmarkCount = count),
                    bookmarked = bookmarked
                )
            }
        }
    }

    fun addComment(content: String) {
        val current = _uiState.value as? CommunityDetailUiState.Success ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createComment(postId, content.trim()).onSuccess { comment ->
                _uiState.value = current.copy(
                    post = current.post.copy(commentCount = current.post.commentCount + 1),
                    comments = current.comments + comment
                )
            }
        }
    }
}
