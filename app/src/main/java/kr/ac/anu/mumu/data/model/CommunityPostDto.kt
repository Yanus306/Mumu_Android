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

data class CommunityRequestDto(
    val category: String,
    val title: String,
    val content: String,
    val hashtags: List<String>,
    val petId: Long? = null
)

data class CommentDto(
    val commentId: Long,
    val communityId: Long,
    val userId: Long,
    val parentCommentId: Long?,
    val content: String,
    val likeCount: Int,
    val createdAt: String,
    val updatedAt: String
)

data class CommentRequestDto(
    val content: String,
    val parentCommentId: Long? = null
)

data class LikeDto(
    val liked: Boolean,
    val likeCount: Int
)

data class BookmarkDto(
    val bookmarked: Boolean,
    val bookmarkCount: Int
)
