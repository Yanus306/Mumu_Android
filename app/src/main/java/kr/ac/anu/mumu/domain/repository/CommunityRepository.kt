package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.CommunityComment
import kr.ac.anu.mumu.domain.model.CommunityPost

interface CommunityRepository {
    suspend fun getPosts(): Result<List<CommunityPost>>
    suspend fun getPost(postId: Long): Result<CommunityPost>
    suspend fun createPost(
        category: String,
        title: String,
        content: String,
        hashtags: List<String>
    ): Result<CommunityPost>
    suspend fun toggleLike(postId: Long): Result<Pair<Boolean, Int>>
    suspend fun toggleBookmark(postId: Long): Result<Pair<Boolean, Int>>
    suspend fun getComments(postId: Long): Result<List<CommunityComment>>
    suspend fun createComment(postId: Long, content: String): Result<CommunityComment>
}
