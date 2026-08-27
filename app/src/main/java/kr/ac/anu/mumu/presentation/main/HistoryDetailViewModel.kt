package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.model.AnalysisHistory
import kr.ac.anu.mumu.domain.repository.HistoryRepository
import javax.inject.Inject

sealed interface HistoryDetailUiState {
    data object Loading : HistoryDetailUiState
    data class Success(val item: AnalysisHistory) : HistoryDetailUiState
    data class Error(val message: String) : HistoryDetailUiState
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val analysisId: Long = checkNotNull(savedStateHandle["analysisId"])
    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = HistoryDetailUiState.Loading
            historyRepository.getBehaviorDetail(analysisId)
                .onSuccess { item -> _uiState.value = HistoryDetailUiState.Success(item) }
                .onFailure { error ->
                    _uiState.value = HistoryDetailUiState.Error(
                        error.message ?: "분석 상세 정보를 불러오지 못했습니다."
                    )
                }
        }
    }
}
