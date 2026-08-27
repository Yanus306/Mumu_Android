package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.CommunityPostDto
import kr.ac.anu.mumu.data.model.PaginatedData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CommunityService {
    @GET("/api/community")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<BaseResponse<PaginatedData<CommunityPostDto>>>
}
