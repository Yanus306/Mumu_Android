package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.CommunityService
import kr.ac.anu.mumu.data.model.CommentDto
import kr.ac.anu.mumu.data.model.CommentRequestDto
import kr.ac.anu.mumu.data.model.CommunityPostDto
import kr.ac.anu.mumu.data.model.CommunityRequestDto
import kr.ac.anu.mumu.domain.model.CommunityComment
import kr.ac.anu.mumu.domain.model.CommunityPost
import kr.ac.anu.mumu.domain.repository.CommunityRepository
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityService: CommunityService
) : CommunityRepository {

    override suspend fun getPosts(): Result<List<CommunityPost>> = runCatching {
        val response = communityService.getPosts()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "게시글을 불러오지 못했습니다. (${response.code()})")
        }
        body.data?.content.orEmpty().map { it.toDomain() }
    }

    override suspend fun getBestPosts(): Result<List<CommunityPost>> = runCatching {
        val response = communityService.getBestPosts()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "인기 게시글을 불러오지 못했습니다. (${response.code()})")
        }
        body.data.orEmpty().map { it.toDomain() }
    }

    override suspend fun getPost(postId: Long): Result<CommunityPost> = runCatching {
        val response = communityService.getPost(postId)
        response.requireData("게시글을 불러오지 못했습니다.").toDomain()
    }

    override suspend fun createPost(
        category: String,
        title: String,
        content: String,
        hashtags: List<String>
    ): Result<CommunityPost> = runCatching {
        val response = communityService.createPost(
            CommunityRequestDto(category, title, content, hashtags)
        )
        response.requireData("게시글을 등록하지 못했습니다.").toDomain()
    }

    override suspend fun toggleLike(postId: Long): Result<Pair<Boolean, Int>> = runCatching {
        val data = communityService.toggleLike(postId).requireData("좋아요 처리에 실패했습니다.")
        data.liked to data.likeCount
    }

    override suspend fun toggleBookmark(postId: Long): Result<Pair<Boolean, Int>> = runCatching {
        val data = communityService.toggleBookmark(postId).requireData("북마크 처리에 실패했습니다.")
        data.bookmarked to data.bookmarkCount
    }

    override suspend fun getComments(postId: Long): Result<List<CommunityComment>> = runCatching {
        val response = communityService.getComments(postId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "댓글을 불러오지 못했습니다. (${response.code()})")
        }
        body.data.orEmpty().map { it.toDomain() }
    }

    override suspend fun createComment(postId: Long, content: String): Result<CommunityComment> = runCatching {
        communityService.createComment(postId, CommentRequestDto(content))
            .requireData("댓글을 등록하지 못했습니다.")
            .toDomain()
    }

    private fun CommunityPostDto.toDomain() = CommunityPost(
        id = communityId,
        userId = userId,
        title = title,
        content = content,
        category = category,
        hashtags = hashtags.orEmpty(),
        thumbnailUrl = thumbnailUrl,
        viewCount = viewCount,
        likeCount = likeCount,
        commentCount = commentCount,
        bookmarkCount = bookmarkCount,
        createdAt = createdAt
    )

    private fun CommentDto.toDomain() = CommunityComment(
        id = commentId,
        userId = userId,
        content = content,
        likeCount = likeCount,
        createdAt = createdAt
    )

    private fun <T> retrofit2.Response<kr.ac.anu.mumu.data.model.BaseResponse<T>>.requireData(
        fallbackMessage: String
    ): T {
        val body = body()
        if (!isSuccessful || body?.success != true) {
            error(body?.message ?: "$fallbackMessage (${code()})")
        }
        return body.data ?: error("서버 응답이 비어 있습니다.")
    }
}
