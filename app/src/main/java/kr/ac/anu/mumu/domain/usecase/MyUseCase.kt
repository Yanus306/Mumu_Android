package kr.ac.anu.mumu.domain.usecase

import kr.ac.anu.mumu.domain.model.MyInformation
import kr.ac.anu.mumu.domain.model.User
import kr.ac.anu.mumu.domain.repository.MyRepository
import javax.inject.Inject

class MyUseCase @Inject constructor(
    private val repository: MyRepository
){
    suspend operator fun invoke(userId: Int): Result<MyInformation> {
        return repository.getUserProfile(userId)
    }
}