package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto

interface PetRepository {
    suspend fun getPets(): Result<List<PetDto>>
    suspend fun savePet(petId: Long?, request: PetRequestDto): Result<PetDto>
    suspend fun deletePet(petId: Long): Result<Unit>
}
