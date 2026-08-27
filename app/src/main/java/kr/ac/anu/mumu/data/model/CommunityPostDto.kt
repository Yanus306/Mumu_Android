package kr.ac.anu.mumu.data.model

data class CommunityPostDto(
    val communityId: Long,
    val userId: Long,
    val petId: Long?,
    val category: String,
    val title: String,
    val content: String,
    val hashtags: List<String>?,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val bookmarkCount: Int,
    val isBest: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String?
)
