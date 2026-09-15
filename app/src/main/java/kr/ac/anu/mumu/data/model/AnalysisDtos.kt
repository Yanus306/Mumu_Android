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
    val name: String?,
    val species: String? = null,
    val breed: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val weight: Double? = null,
    val neutered: Boolean = false,
    val allergies: String? = null,
    val chronicDiseases: String? = null,
    val medications: String? = null,
    val profileImageUrl: String? = null,
    val ageYears: Int? = null,
    val recordDays: Int? = null,
    val speciesLabel: String? = null,
    val likeCount: Long? = null
)

data class PetRequestDto(
    val name: String,
    val species: String,
    val breed: String?,
    val gender: String?,
    val birthDate: String?,
    val weight: Double?,
    val neutered: Boolean,
    val allergies: String?,
    val chronicDiseases: String?,
    val medications: String?
)
