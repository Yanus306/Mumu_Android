package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.AnalysisHistory

interface HistoryRepository {
    suspend fun getBehaviorHistory(): Result<List<AnalysisHistory>>
    suspend fun getBehaviorDetail(analysisId: Long): Result<AnalysisHistory>
}
