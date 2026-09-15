package kr.ac.anu.mumu.data.model

data class HospitalListDto(
    val hospitalId: Long,
    val name: String,
    val address: String?,
    val phone: String?,
    val latitude: Double?,
    val longitude: Double?,
    val distanceKm: Double?,
    val averageRating: Double?,
    val totalReviews: Int?,
    val thumbnailUrl: String?
)

data class HospitalDetailDto(
    val hospitalId: Long,
    val name: String,
    val address: String?,
    val phone: String?,
    val website: String?,
    val openingHours: Any?,
    val specialties: List<String>?,
    val averageRating: Double?,
    val totalReviews: Int?,
    val imageUrls: List<String>?
)

data class HospitalPriceDto(
    val hospitalPriceId: Long,
    val treatmentType: String,
    val species: String?,
    val minPrice: Double?,
    val maxPrice: Double?,
    val averagePrice: Double?,
    val description: String?
)

data class HospitalReviewDto(
    val reviewId: Long,
    val userId: Long,
    val rating: Int,
    val content: String?,
    val createdAt: String?
)
