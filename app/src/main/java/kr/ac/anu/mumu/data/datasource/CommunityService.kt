package kr.ac.anu.mumu.data.datasource

import com.google.gson.JsonObject
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.BookmarkDto
import kr.ac.anu.mumu.data.model.CommentDto
import kr.ac.anu.mumu.data.model.CommentRequestDto
import kr.ac.anu.mumu.data.model.CommunityPostDto
import kr.ac.anu.mumu.data.model.CommunityRequestDto
import kr.ac.anu.mumu.data.model.LikeDto
import kr.ac.anu.mumu.data.model.PaginatedData
import kr.ac.anu.mumu.data.model.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityService {
    @GET("/api/community")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<BaseResponse<PaginatedData<CommunityPostDto>>>

    @GET("/api/community/best")
    suspend fun getBestPosts(): Response<BaseResponse<List<CommunityPostDto>>>

    @GET("/api/community/{communityId}")
    suspend fun getPost(
        @Path("communityId") communityId: Long
    ): Response<BaseResponse<CommunityPostDto>>

    @POST("/api/community")
    suspend fun createPost(
        @Body request: CommunityRequestDto
    ): Response<BaseResponse<CommunityPostDto>>

    @PUT("/api/community/{communityId}")
    suspend fun updatePost(
        @Path("communityId") communityId: Long,
        @Body request: CommunityRequestDto
    ): Response<BaseResponse<CommunityPostDto>>

    @DELETE("/api/community/{communityId}")
    suspend fun deletePost(
        @Path("communityId") communityId: Long
    ): Response<BaseResponse<JsonObject>>

    @POST("/api/likes/community/{communityId}")
    suspend fun toggleLike(
        @Path("communityId") communityId: Long
    ): Response<BaseResponse<LikeDto>>

    @POST("/api/bookmarks/{communityId}")
    suspend fun toggleBookmark(
        @Path("communityId") communityId: Long
    ): Response<BaseResponse<BookmarkDto>>

    @GET("/api/community/{communityId}/comments")
    suspend fun getComments(
        @Path("communityId") communityId: Long
    ): Response<BaseResponse<List<CommentDto>>>

    @POST("/api/community/{communityId}/comments")
    suspend fun createComment(
        @Path("communityId") communityId: Long,
        @Body request: CommentRequestDto
    ): Response<BaseResponse<CommentDto>>

    @PUT("/api/community/{communityId}/comments/{commentId}")
    suspend fun updateComment(
        @Path("communityId") communityId: Long,
        @Path("commentId") commentId: Long,
        @Body request: CommentRequestDto
    ): Response<BaseResponse<CommentDto>>

    @DELETE("/api/community/{communityId}/comments/{commentId}")
    suspend fun deleteComment(
        @Path("communityId") communityId: Long,
        @Path("commentId") commentId: Long
    ): Response<BaseResponse<JsonObject>>

    @GET("/api/users/profile")
    suspend fun getMyProfile(): Response<BaseResponse<UserProfileDto>>
}
