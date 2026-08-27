package kr.ac.anu.mumu.domain.model

data class BehaviorAnalysisResult(
    val videoUri: String,
    val isNormal: Boolean,
    val probability: Int,
    val suspectedBehavior: String,
    val reason: String,
    val recommendation: String
)
