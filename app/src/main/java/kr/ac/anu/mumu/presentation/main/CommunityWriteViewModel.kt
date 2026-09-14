package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.model.CommunityPost
import kr.ac.anu.mumu.domain.repository.CommunityRepository
import javax.inject.Inject

sealed interface CommunityWriteUiState {
    data object Loading : CommunityWriteUiState
    data class Ready(val post: CommunityPost? = null) : CommunityWriteUiState
    data object Submitting : CommunityWriteUiState
    data class Success(val postId: Long, val isEdit: Boolean) : CommunityWriteUiState
    data class Error(val message: String) : CommunityWriteUiState
}

@HiltViewModel
class CommunityWriteViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CommunityRepository
) : ViewModel() {

    private val editingPostId = savedStateHandle.get<Long>("postId")?.takeIf { it > 0L }
    private var editingPost: CommunityPost? = null
    val isEditMode: Boolean = editingPostId != null
    val needsEditPostLoad: Boolean get() = isEditMode && editingPost == null

    private val _uiState = MutableStateFlow<CommunityWriteUiState>(
        if (isEditMode) CommunityWriteUiState.Loading else CommunityWriteUiState.Ready()
    )
    val uiState: StateFlow<CommunityWriteUiState> = _uiState.asStateFlow()

    init {
        if (isEditMode) loadPost()
    }

    fun loadPost() {
        val postId = editingPostId ?: return
        _uiState.value = CommunityWriteUiState.Loading
        viewModelScope.launch {
            repository.getPost(postId)
                .onSuccess { post ->
                    editingPost = post
                    _uiState.value = CommunityWriteUiState.Ready(post)
                }
                .onFailure { error ->
                    _uiState.value = CommunityWriteUiState.Error(
                        error.message ?: "게시글을 불러오지 못했습니다."
                    )
                }
        }
    }

    fun submit(category: String, title: String, content: String, hashtags: String) {
        if (_uiState.value is CommunityWriteUiState.Submitting || needsEditPostLoad) return
        CommunityInputValidator.validatePost(title, content)?.let { message ->
            _uiState.value = CommunityWriteUiState.Error(message)
            return
        }
        val hashtagList = hashtags.split(',', ' ', '#')
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

        viewModelScope.launch {
            _uiState.value = CommunityWriteUiState.Submitting
            val result = editingPostId?.let { postId ->
                repository.updatePost(
                    postId = postId,
                    category = category,
                    title = title.trim(),
                    content = content.trim(),
                    hashtags = hashtagList,
                    petId = editingPost?.petId
                )
            } ?: repository.createPost(category, title.trim(), content.trim(), hashtagList)

            result
                .onSuccess { post ->
                    _uiState.value = CommunityWriteUiState.Success(post.id, isEditMode)
                }
                .onFailure { error ->
                    _uiState.value = CommunityWriteUiState.Error(
                        error.message ?: if (isEditMode) {
                            "게시글을 수정하지 못했습니다."
                        } else {
                            "게시글을 등록하지 못했습니다."
                        }
                    )
                }
        }
    }
}
