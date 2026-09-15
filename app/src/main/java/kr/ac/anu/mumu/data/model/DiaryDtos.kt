package kr.ac.anu.mumu.data.model

data class UploadResponseDto(val key: String?, val url: String?)

data class DiaryRequestDto(
    val petId: Long,
    val mood: String,
    val title: String,
    val content: String,
    val diaryDate: String,
    val imageKeys: List<String>? = null,
    val behaviorAnalysisId: Long? = null,
    val soundAnalysisId: Long? = null,
    val foodSafetyAnalysisId: Long? = null
)

data class DiaryAnalysisSummaryDto(
    val analysisId: Long,
    val type: String,
    val resultLabel: String?
)

data class DiaryListDto(
    val diaryId: Long,
    val mood: String,
    val title: String,
    val contentPreview: String?,
    val diaryDate: String,
    val thumbnailUrl: String?
)

data class DiaryDetailDto(
    val diaryId: Long,
    val petId: Long,
    val mood: String,
    val title: String,
    val content: String,
    val diaryDate: String,
    val imageUrls: List<String>?,
    val analysisSummary: DiaryAnalysisSummaryDto?
)

data class DiaryCalendarDto(
    val petId: Long,
    val year: Int,
    val month: Int,
    val writtenDates: List<String>
)
