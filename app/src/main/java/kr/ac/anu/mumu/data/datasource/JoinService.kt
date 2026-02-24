package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.JoinRequest
import kr.ac.anu.mumu.data.model.JoinResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface JoinService {
    @POST("/api/users/register")
    suspend fun registerUser(
        @Body request: JoinRequest
    ): Response<JoinResponse>
}
