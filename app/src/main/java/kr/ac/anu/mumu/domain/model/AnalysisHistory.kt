package kr.ac.anu.mumu.domain.model

data class AnalysisHistory(
    val id: Long,
    val isNormal: Boolean,
    val behaviorText: String,
    val probability: Int,
    val date: String
)