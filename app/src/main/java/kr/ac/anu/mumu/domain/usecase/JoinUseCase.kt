package kr.ac.anu.mumu.domain.usecase

import kr.ac.anu.mumu.data.model.JoinRequest
import kr.ac.anu.mumu.domain.repository.JoinRepository
import javax.inject.Inject

class JoinUseCase @Inject constructor(
    private val repository: JoinRepository
) {
    suspend operator fun invoke(request: JoinRequest): Result<Unit> {
        return repository.register(request)
    }
}