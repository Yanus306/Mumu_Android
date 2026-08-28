package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.repository.CommunityRepository
import javax.inject.Inject

sealed interface CommunityWriteUiState {
    data object Idle : CommunityWriteUiState
    data object Loading : CommunityWriteUiState
    data class Success(val postId: Long) : CommunityWriteUiState
    data class Error(val message: String) : CommunityWriteUiState
}

@HiltViewModel
class CommunityWriteViewModel @Inject constructor(
    private val repository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityWriteUiState>(CommunityWriteUiState.Idle)
    val uiState: StateFlow<CommunityWriteUiState> = _uiState.asStateFlow()

    fun submit(category: String, title: String, content: String, hashtags: String) {
        if (title.isBlank() || content.isBlank()) {
            _uiState.value = CommunityWriteUiState.Error("제목과 내용을 입력해 주세요.")
            return
        }
        val hashtagList = hashtags.split(',', ' ', '#')
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()

        viewModelScope.launch {
            _uiState.value = CommunityWriteUiState.Loading
            repository.createPost(category, title.trim(), content.trim(), hashtagList)
                .onSuccess { post -> _uiState.value = CommunityWriteUiState.Success(post.id) }
                .onFailure { error ->
                    _uiState.value = CommunityWriteUiState.Error(
                        error.message ?: "게시글을 등록하지 못했습니다."
                    )
                }
        }
    }
}
