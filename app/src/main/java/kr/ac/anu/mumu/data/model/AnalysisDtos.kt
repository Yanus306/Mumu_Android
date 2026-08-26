package kr.ac.anu.mumu.data.model

data class AnalysisTriggerDto(
    val analysisId: Long,
    val status: String
)

data class AnalysisDetailDto(
    val analysisId: Long,
    val type: String?,
    val status: String,
    val resultLabel: String?,
    val suspectedItems: List<String>?,
    val confidence: Double?,
    val analyzedAt: String?
)

data class PetDto(
    val petId: Long,
    val name: String?
)
