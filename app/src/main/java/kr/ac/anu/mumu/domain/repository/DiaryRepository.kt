package kr.ac.anu.mumu.domain.repository

import kr.ac.anu.mumu.data.model.DiaryCalendarDto
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.data.model.PaginatedData

interface DiaryRepository {
    suspend fun getCalendar(petId: Long, year: Int, month: Int): Result<DiaryCalendarDto>
    suspend fun getDiaries(petId: Long, page: Int): Result<PaginatedData<DiaryListDto>>
    suspend fun getDiary(diaryId: Long): Result<DiaryDetailDto>
    suspend fun saveDiary(diaryId: Long?, request: DiaryRequestDto): Result<DiaryDetailDto>
    suspend fun deleteDiary(diaryId: Long): Result<Unit>
}
