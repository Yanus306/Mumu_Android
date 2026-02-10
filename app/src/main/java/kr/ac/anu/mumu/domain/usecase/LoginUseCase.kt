package kr.ac.anu.mumu.domain.usecase

import kr.ac.anu.mumu.domain.model.User
import kr.ac.anu.mumu.domain.repository.LoginRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: LoginRepository
) {
    suspend operator fun invoke(id: String, pw: String): Result<User> {
        return repository.login(id, pw)
    }
}