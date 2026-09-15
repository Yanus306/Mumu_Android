package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.HospitalDetailDto
import kr.ac.anu.mumu.data.model.HospitalListDto
import kr.ac.anu.mumu.data.model.HospitalPriceDto
import kr.ac.anu.mumu.data.model.HospitalReviewDto
import kr.ac.anu.mumu.data.model.PaginatedData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface HospitalService {
    @GET("/api/hospitals")
    suspend fun search(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double = 20.0,
        @Query("keyword") keyword: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<BaseResponse<PaginatedData<HospitalListDto>>>

    @GET("/api/hospitals/{hospitalId}")
    suspend fun getDetail(@Path("hospitalId") hospitalId: Long): Response<BaseResponse<HospitalDetailDto>>

    @GET("/api/hospitals/{hospitalId}/prices")
    suspend fun getPrices(@Path("hospitalId") hospitalId: Long): Response<BaseResponse<List<HospitalPriceDto>>>

    @GET("/api/hospitals/{hospitalId}/reviews")
    suspend fun getReviews(
        @Path("hospitalId") hospitalId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<BaseResponse<PaginatedData<HospitalReviewDto>>>
}
