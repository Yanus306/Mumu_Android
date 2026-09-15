package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.HospitalService
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.HospitalDetailDto
import kr.ac.anu.mumu.data.model.HospitalListDto
import kr.ac.anu.mumu.data.model.HospitalPriceDto
import kr.ac.anu.mumu.data.model.HospitalReviewDto
import kr.ac.anu.mumu.data.model.PaginatedData
import kr.ac.anu.mumu.domain.repository.HospitalRepository
import retrofit2.Response
import javax.inject.Inject

class HospitalRepositoryImpl @Inject constructor(private val service: HospitalService) : HospitalRepository {
    override suspend fun search(keyword: String?, page: Int): Result<PaginatedData<HospitalListDto>> = runCatching {
        service.search(ANDONG_LAT, ANDONG_LNG, keyword = keyword, page = page).requireData("병원을 찾지 못했습니다.")
    }

    override suspend fun getDetail(hospitalId: Long): Result<HospitalDetailDto> = runCatching {
        service.getDetail(hospitalId).requireData("병원 정보를 불러오지 못했습니다.")
    }

    override suspend fun getPrices(hospitalId: Long): Result<List<HospitalPriceDto>> = runCatching {
        service.getPrices(hospitalId).requireData("진료비를 불러오지 못했습니다.")
    }

    override suspend fun getReviews(hospitalId: Long): Result<List<HospitalReviewDto>> = runCatching {
        service.getReviews(hospitalId).requireData("리뷰를 불러오지 못했습니다.").content
    }

    private fun <T> Response<BaseResponse<T>>.requireData(message: String): T {
        val body = body()
        if (!isSuccessful || body?.success != true) error(body?.message ?: "$message (${code()})")
        return body.data ?: error("서버 응답이 비어 있습니다.")
    }

    private companion object {
        const val ANDONG_LAT = 36.568188
        const val ANDONG_LNG = 128.730261
    }
}
