package kr.ac.anu.mumu.presentation.main

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

sealed interface PostUiState {
    data object Loading : PostUiState
    data class Success(val posts: List<CommunityPost>) : PostUiState
    data class Error(val message: String) : PostUiState
}

@HiltViewModel
class PostViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.Loading)
    val uiState: StateFlow<PostUiState> = _uiState.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            _uiState.value = PostUiState.Loading
            communityRepository.getPosts()
                .onSuccess { posts -> _uiState.value = PostUiState.Success(posts) }
                .onFailure { error ->
                    _uiState.value = PostUiState.Error(
                        error.message ?: "게시글을 불러오지 못했습니다."
                    )
                }
        }
    }
}
