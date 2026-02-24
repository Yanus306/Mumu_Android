package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.data.model.JoinRequest

interface JoinRepository {
    suspend fun register(request: JoinRequest): Result<Unit>
}