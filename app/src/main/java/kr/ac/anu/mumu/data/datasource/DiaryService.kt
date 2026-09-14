package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.data.model.PaginatedData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface DiaryService {
    @GET("/api/diaries")
    suspend fun getDiaries(
        @Query("petId") petId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<BaseResponse<PaginatedData<DiaryListDto>>>

    @GET("/api/diaries/{diaryId}")
    suspend fun getDiary(@Path("diaryId") diaryId: Long): Response<BaseResponse<DiaryDetailDto>>

    @POST("/api/diaries")
    suspend fun createDiary(@Body request: DiaryRequestDto): Response<BaseResponse<DiaryDetailDto>>

    @PUT("/api/diaries/{diaryId}")
    suspend fun updateDiary(
        @Path("diaryId") diaryId: Long,
        @Body request: DiaryRequestDto
    ): Response<BaseResponse<DiaryDetailDto>>

    @DELETE("/api/diaries/{diaryId}")
    suspend fun deleteDiary(@Path("diaryId") diaryId: Long): Response<Unit>
}
