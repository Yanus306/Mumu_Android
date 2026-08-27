package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.AnalysisService
import kr.ac.anu.mumu.data.datasource.HistoryService
import kr.ac.anu.mumu.data.datasource.PetService
import kr.ac.anu.mumu.data.mapper.toDomain
import kr.ac.anu.mumu.data.model.AnalysisHistoryDto
import kr.ac.anu.mumu.domain.model.AnalysisHistory
import kr.ac.anu.mumu.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val historyService: HistoryService,
    private val analysisService: AnalysisService,
    private val petService: PetService
) : HistoryRepository {

    override suspend fun getBehaviorHistory(): Result<List<AnalysisHistory>> = runCatching {
        val petId = getPrimaryPetId()
        val response = historyService.getAnalysisHistory(petId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "분석 내역을 불러오지 못했습니다. (${response.code()})")
        }
        body.data?.content.orEmpty().map(AnalysisHistoryDto::toDomain)
    }

    override suspend fun getBehaviorDetail(analysisId: Long): Result<AnalysisHistory> = runCatching {
        val response = analysisService.getBehaviorAnalysis(analysisId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "분석 상세 정보를 불러오지 못했습니다. (${response.code()})")
        }
        val detail = body.data ?: error("분석 상세 정보가 비어 있습니다.")
        AnalysisHistoryDto(
            analysisId = detail.analysisId,
            type = detail.type.orEmpty(),
            status = detail.status,
            resultLabel = detail.resultLabel,
            suspectedItems = detail.suspectedItems,
            confidence = detail.confidence,
            analyzedAt = detail.analyzedAt
        ).toDomain()
    }

    private suspend fun getPrimaryPetId(): Long {
        val response = petService.getMyPets()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "반려동물 정보를 불러오지 못했습니다. (${response.code()})")
        }
        return body.data?.firstOrNull()?.petId
            ?: error("분석 내역을 확인할 반려동물을 먼저 등록해 주세요.")
    }
}
