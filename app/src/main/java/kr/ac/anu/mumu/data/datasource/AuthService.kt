package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.LoginRequest
import kr.ac.anu.mumu.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("/api/users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
