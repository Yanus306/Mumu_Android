package kr.ac.anu.mumu.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    fun analyze(videoUri: String) {
        val currentState = _uiState.value
        if (currentState is AnalysisUiState.Loading && currentState.videoUri == videoUri) return
        if (currentState is AnalysisUiState.Success && currentState.result.videoUri == videoUri) return

        viewModelScope.launch {
            _uiState.value = AnalysisUiState.Loading(videoUri)
            analysisRepository.analyzeBehavior(videoUri)
                .onSuccess { result -> _uiState.value = AnalysisUiState.Success(result) }
                .onFailure { error ->
                    _uiState.value = AnalysisUiState.Error(
                        videoUri = videoUri,
                        message = error.message ?: "분석 중 오류가 발생했습니다."
                    )
                }
        }
    }
}
