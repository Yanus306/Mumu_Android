package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.JoinService
import kr.ac.anu.mumu.data.model.JoinRequest
import kr.ac.anu.mumu.domain.repository.JoinRepository
import org.json.JSONObject
import javax.inject.Inject

class JoinRepositoryImpl @Inject constructor(
    private val joinService: JoinService
) : JoinRepository {
    override suspend fun register(request: JoinRequest): Result<Unit> {
        return try {
            val response = joinService.registerUser(request)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorString = response.errorBody()?.string()
                val errorMessage = try {
                    JSONObject(errorString ?: "").optString("message", "회원가입에 실패했습니다.")
                } catch (e: Exception) {
                    "회원가입에 실패했습니다."
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
