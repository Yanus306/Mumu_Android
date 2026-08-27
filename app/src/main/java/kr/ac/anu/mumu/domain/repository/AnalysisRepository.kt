package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult

interface AnalysisRepository {
    suspend fun analyzeBehavior(videoUri: String): Result<BehaviorAnalysisResult>
}
