package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.User

interface LoginRepository {
    suspend fun login(id: String, pw: String) : Result<User>
}