package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.AuthService
import kr.ac.anu.mumu.data.local.SessionManager
import kr.ac.anu.mumu.data.model.LoginRequest
import kr.ac.anu.mumu.domain.model.User
import kr.ac.anu.mumu.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    private val sessionManager: SessionManager
) : LoginRepository {

    override suspend fun login(id: String, pw: String): Result<User> {
        return try {
            val response = authService.login(LoginRequest(loginId = id, password = pw))

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!

                if (result.success) {
                    sessionManager.saveTokens(
                        accessToken = result.data.accessToken,
                        refreshToken = result.data.refreshToken,
                        tokenType = result.data.tokenType
                    )
                    val domainUser = User(
                        accessToken = result.data.accessToken,
                        refreshToken = result.data.refreshToken,
                        tokenType = result.data.tokenType
                    )
                    Result.success(domainUser)
                } else {
                    Result.failure(Exception(result.message))
                }
            } else {
                Result.failure(Exception("통신 오류 : ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
