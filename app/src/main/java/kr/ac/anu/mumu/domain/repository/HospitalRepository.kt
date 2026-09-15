package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.data.model.HospitalDetailDto
import kr.ac.anu.mumu.data.model.HospitalListDto
import kr.ac.anu.mumu.data.model.HospitalPriceDto
import kr.ac.anu.mumu.data.model.HospitalReviewDto
import kr.ac.anu.mumu.data.model.PaginatedData

interface HospitalRepository {
    suspend fun search(keyword: String?, page: Int): Result<PaginatedData<HospitalListDto>>
    suspend fun getDetail(hospitalId: Long): Result<HospitalDetailDto>
    suspend fun getPrices(hospitalId: Long): Result<List<HospitalPriceDto>>
    suspend fun getReviews(hospitalId: Long): Result<List<HospitalReviewDto>>
}
