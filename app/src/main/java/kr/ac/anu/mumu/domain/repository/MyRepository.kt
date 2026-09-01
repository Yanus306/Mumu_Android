package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.domain.model.MyInformation

interface MyRepository {
    suspend fun getUserProfile(userId: Int): Result<MyInformation>
}