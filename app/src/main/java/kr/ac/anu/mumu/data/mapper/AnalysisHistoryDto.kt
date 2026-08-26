package kr.ac.anu.mumu.data.mapper

import kr.ac.anu.mumu.data.model.AnalysisHistoryDto
import kr.ac.anu.mumu.domain.model.AnalysisHistory
import java.text.SimpleDateFormat
import java.util.Locale

fun AnalysisHistoryDto.toDomain(): AnalysisHistory {
    val formattedDate = try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
        val parsedDate = parser.parse(this.analyzedAt)
        parsedDate?.let { formatter.format(it) } ?: this.analyzedAt
    } catch (e: Exception) {
        this.analyzedAt
    }

    val isNormal = this.status == "정상" || this.resultLabel?.uppercase() == "NORMAL"

    val joinedBehavior = if (this.suspectedItems.isNullOrEmpty()) {
        "없음"
    } else {
        this.suspectedItems.joinToString(", ")
    }

    return AnalysisHistory(
        id = this.analysisId,
        isNormal = isNormal,
        behaviorText = joinedBehavior,
        probability = this.confidence,
        date = formattedDate
    )
}