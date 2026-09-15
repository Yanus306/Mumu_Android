package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.PetService
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto
import kr.ac.anu.mumu.domain.repository.PetRepository
import retrofit2.Response
import javax.inject.Inject

class PetRepositoryImpl @Inject constructor(
    private val service: PetService
) : PetRepository {
    override suspend fun getPets(): Result<List<PetDto>> = runCatching {
        service.getMyPets().requireData("반려동물을 불러오지 못했습니다.")
    }

    override suspend fun savePet(petId: Long?, request: PetRequestDto): Result<PetDto> = runCatching {
        val response = if (petId == null) service.createPet(request) else service.updatePet(petId, request)
        response.requireData("반려동물을 저장하지 못했습니다.")
    }

    override suspend fun deletePet(petId: Long): Result<Unit> = runCatching {
        val response = service.deletePet(petId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "반려동물을 삭제하지 못했습니다. (${response.code()})")
        }
    }

    private fun <T> Response<BaseResponse<T>>.requireData(message: String): T {
        val body = body()
        if (!isSuccessful || body?.success != true) error(body?.message ?: "$message (${code()})")
        return body.data ?: error("서버 응답이 비어 있습니다.")
    }
}
