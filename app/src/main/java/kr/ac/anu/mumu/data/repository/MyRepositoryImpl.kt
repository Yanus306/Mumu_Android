package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.MyService
import kr.ac.anu.mumu.domain.model.MyInformation
import kr.ac.anu.mumu.domain.repository.MyRepository
import javax.inject.Inject

class MyRepositoryImpl @Inject constructor(
    private val myService: MyService
) : MyRepository {

    override suspend fun getUserProfile(userId: Int): Result<MyInformation> {
        return try {
            val response = myService.getUserProfile(userId)

            val myInfo = MyInformation(
                id = response.user.loginId,
                name = response.user.name,
                phone = response.user.phone
            )

            Result.success(myInfo)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}