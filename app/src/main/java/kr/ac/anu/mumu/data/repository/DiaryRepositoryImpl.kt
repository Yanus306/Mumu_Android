package kr.ac.anu.mumu.data.repository

import kr.ac.anu.mumu.data.datasource.DiaryService
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.DiaryCalendarDto
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.data.model.PaginatedData
import kr.ac.anu.mumu.domain.repository.DiaryRepository
import retrofit2.Response
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    private val service: DiaryService
) : DiaryRepository {
    override suspend fun getCalendar(petId: Long, year: Int, month: Int): Result<DiaryCalendarDto> = runCatching {
        service.getCalendar(petId, year, month).requireData("일기 달력을 불러오지 못했습니다.")
    }

    override suspend fun getDiaries(petId: Long, page: Int): Result<PaginatedData<DiaryListDto>> = runCatching {
        service.getDiaries(petId, page).requireData("일기를 불러오지 못했습니다.")
    }

    override suspend fun getDiary(diaryId: Long): Result<DiaryDetailDto> = runCatching {
        service.getDiary(diaryId).requireData("일기를 불러오지 못했습니다.")
    }

    override suspend fun saveDiary(diaryId: Long?, request: DiaryRequestDto): Result<DiaryDetailDto> = runCatching {
        val response = if (diaryId == null) service.createDiary(request) else service.updateDiary(diaryId, request)
        response.requireData("일기를 저장하지 못했습니다.")
    }

    override suspend fun deleteDiary(diaryId: Long): Result<Unit> = runCatching {
        val response = service.deleteDiary(diaryId)
        if (!response.isSuccessful) error("일기를 삭제하지 못했습니다. (${response.code()})")
    }

    private fun <T> Response<BaseResponse<T>>.requireData(message: String): T {
        val body = body()
        if (!isSuccessful || body?.success != true) error(body?.message ?: "$message (${code()})")
        return body.data ?: error("서버 응답이 비어 있습니다.")
    }
}
