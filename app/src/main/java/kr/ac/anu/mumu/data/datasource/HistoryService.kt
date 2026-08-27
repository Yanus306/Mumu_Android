package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.AnalysisHistoryDto
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.PaginatedData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HistoryService {
    @GET("/api/pets/{petId}/analyses")
    suspend fun getAnalysisHistory(
        @Path("petId") petId: Long,
        @Query("type") type: String = "behavior",
        @Query("status") status: String = "completed",
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): Response<BaseResponse<PaginatedData<AnalysisHistoryDto>>>
}
