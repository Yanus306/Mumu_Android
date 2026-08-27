package kr.ac.anu.mumu.presentation.analysis

import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult

internal const val ARG_VIDEO_URI = "analysis_video_uri"

sealed interface AnalysisUiState {
    data object Idle : AnalysisUiState
    data class Loading(val videoUri: String) : AnalysisUiState
    data class Success(val result: BehaviorAnalysisResult) : AnalysisUiState
    data class Error(val videoUri: String, val message: String) : AnalysisUiState
}
