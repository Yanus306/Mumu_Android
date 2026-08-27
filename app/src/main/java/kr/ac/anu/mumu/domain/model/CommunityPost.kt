package kr.ac.anu.mumu.domain.model

data class CommunityPost(
    val id: Long,
    val userId: Long,
    val title: String,
    val content: String,
    val category: String,
    val hashtags: List<String>,
    val thumbnailUrl: String?,
    val viewCount: Int,
    val likeCount: Int,
    val commentCount: Int,
    val bookmarkCount: Int,
    val createdAt: String
)

data class CommunityComment(
    val id: Long,
    val userId: Long,
    val content: String,
    val likeCount: Int,
    val createdAt: String
)
