package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.CommunityService
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
        body.data?.content.orEmpty().map { post ->
            CommunityPost(
                id = post.communityId,
                title = post.title,
                thumbnailUrl = post.thumbnailUrl
            )
        }
    }
}
