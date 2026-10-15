package kr.ac.anu.mumu.presentation.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()
    private var activeRequestId: String? = null
    private var analysisJob: Job? = null
    private var attempt = 0L

    /** A restored loading screen observes its existing request instead of uploading again. */
    fun analyze(videoUri: String, requestId: String, retry: Boolean = false, demo: Boolean = AnalysisConfig.DEMO_MODE) {
        if (activeRequestId == requestId && !retry) return
        if (activeRequestId == requestId && analysisJob?.isActive == true) return

        analysisJob?.cancel()
        activeRequestId = requestId
        val currentAttempt = ++attempt
        _uiState.value = AnalysisUiState.Loading(videoUri)
        analysisJob = viewModelScope.launch {
            if (demo) {
                delay(AnalysisConfig.DEMO_LOADING_MILLIS)
                if (currentAttempt == attempt) showDemoResult(videoUri)
                return@launch
            }
            val result = analysisRepository.analyzeBehavior(videoUri)
            coroutineContext.ensureActive()
            if (currentAttempt != attempt) return@launch
            result
                .onSuccess { result -> _uiState.value = AnalysisUiState.Success(result) }
                .onFailure { error ->
                    _uiState.value = AnalysisUiState.Error(
                        videoUri = videoUri,
                        message = error.message ?: "분석 중 오류가 발생했습니다."
                    )
                }
        }
    }

    /** Temporary presentation path. Remove or set false in AnalysisConfig when server demo is ready. */
    private fun showDemoResult(videoUri: String) {
        _uiState.value = AnalysisUiState.Success(
            BehaviorAnalysisResult(
                videoUri = videoUri,
                isNormal = false,
                probability = 72,
                suspectedBehavior = "발바닥 이상 행동",
                reason = "영상에서 반려동물의 행동 패턴을 확인했어요.",
                recommendation = "같은 행동이 반복되면 가까운 동물병원에서 상담해 주세요."
            )
        )
    }

    fun isCurrentRequest(requestId: String): Boolean = activeRequestId == requestId
}
