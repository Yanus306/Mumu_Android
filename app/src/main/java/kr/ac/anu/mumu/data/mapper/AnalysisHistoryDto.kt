package kr.ac.anu.mumu.data.mapper

import kr.ac.anu.mumu.data.model.AnalysisHistoryDto
import kr.ac.anu.mumu.domain.model.AnalysisHistory
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun AnalysisHistoryDto.toDomain(): AnalysisHistory {
    val formattedDate = analyzedAt?.let { value ->
        runCatching {
            OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        }.getOrDefault(value.take(10).replace('-', '.'))
    } ?: "-"

    val probability = confidence?.roundToInt()?.coerceIn(0, 100) ?: 0
    val isNormal = resultLabel.equals("NORMAL", ignoreCase = true) ||
        resultLabel == "정상" ||
        (suspectedItems.isNullOrEmpty() && probability < 50)

    val joinedBehavior = if (suspectedItems.isNullOrEmpty()) {
        if (isNormal) "특이 행동 없음" else resultLabel ?: "분석 결과 확인"
    } else {
        suspectedItems.joinToString(", ")
    }

    return AnalysisHistory(
        id = this.analysisId,
        isNormal = isNormal,
        behaviorText = joinedBehavior,
        probability = probability,
        date = formattedDate,
        resultLabel = resultLabel
    )
}
