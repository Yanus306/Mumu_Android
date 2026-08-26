package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.PetDto
import retrofit2.Response
import retrofit2.http.GET

interface PetService {
    @GET("/api/pets")
    suspend fun getMyPets(): Response<BaseResponse<List<PetDto>>>
}
