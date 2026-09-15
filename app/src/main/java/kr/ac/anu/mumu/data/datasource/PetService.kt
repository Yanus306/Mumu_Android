package kr.ac.anu.mumu.data.datasource

import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PetService {
    @GET("/api/pets")
    suspend fun getMyPets(): Response<BaseResponse<List<PetDto>>>

    @POST("/api/pets")
    suspend fun createPet(@Body request: PetRequestDto): Response<BaseResponse<PetDto>>

    @PUT("/api/pets/{petId}")
    suspend fun updatePet(
        @Path("petId") petId: Long,
        @Body request: PetRequestDto
    ): Response<BaseResponse<PetDto>>

    @DELETE("/api/pets/{petId}")
    suspend fun deletePet(@Path("petId") petId: Long): Response<BaseResponse<Unit>>
}
