package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.MyResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface MyService {
    @GET("/api/users/profile/{id}")
    suspend fun getUserProfile(@Path("id") userId: Int): MyResponse
}