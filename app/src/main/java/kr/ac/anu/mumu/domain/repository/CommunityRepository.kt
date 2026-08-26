package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.CommunityPost

interface CommunityRepository {
    suspend fun getPosts(): Result<List<CommunityPost>>
}
