package kr.ac.anu.mumu.data.model

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PaginatedData<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class AnalysisHistoryDto(
    val analysisId: Long,
    val type: String,
    val status: String,
    val resultLabel: String?,
    val suspectedItems: List<String>?,
    val confidence: Double?,
    val analyzedAt: String?
)
