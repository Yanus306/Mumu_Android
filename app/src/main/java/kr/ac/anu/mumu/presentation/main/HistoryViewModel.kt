package kr.ac.anu.mumu.presentation.main

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

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(val items: List<AnalysisHistory>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState.Loading
            historyRepository.getBehaviorHistory()
                .onSuccess { items -> _uiState.value = HistoryUiState.Success(items) }
                .onFailure { error ->
                    _uiState.value = HistoryUiState.Error(
                        error.message ?: "분석 내역을 불러오지 못했습니다."
                    )
                }
        }
    }
}
