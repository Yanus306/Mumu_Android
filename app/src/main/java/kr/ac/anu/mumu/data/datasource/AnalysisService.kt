package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.AnalysisDetailDto
import kr.ac.anu.mumu.data.model.AnalysisTriggerDto
import kr.ac.anu.mumu.data.model.BaseResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AnalysisService {
    @Multipart
    @POST("/api/behavior-analyses")
    suspend fun triggerBehaviorAnalysis(
        @Query("petId") petId: Long,
        @Part video: MultipartBody.Part
    ): Response<BaseResponse<AnalysisTriggerDto>>

    @GET("/api/behavior-analyses/{id}")
    suspend fun getBehaviorAnalysis(
        @Path("id") analysisId: Long
    ): Response<BaseResponse<AnalysisDetailDto>>
}
