package kr.ac.anu.mumu.data.repository

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.ac.anu.mumu.data.datasource.DiaryService
import kr.ac.anu.mumu.data.model.BaseResponse
import kr.ac.anu.mumu.data.model.DiaryCalendarDto
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.data.model.PaginatedData
import kr.ac.anu.mumu.domain.repository.DiaryRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class DiaryRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val service: DiaryService
) : DiaryRepository {
    override suspend fun uploadDiaryImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val type = context.contentResolver.getType(uri) ?: error("사진 형식을 확인할 수 없습니다.")
            if (type !in setOf("image/jpeg", "image/png", "image/webp")) {
                error("JPG, PNG, WebP 사진만 첨부할 수 있습니다.")
            }
            val bytes = readLimitedImage(uri)
            val extension = when (type) {
                "image/png" -> "png"
                "image/webp" -> "webp"
                else -> "jpg"
            }
            val part = MultipartBody.Part.createFormData(
                "file",
                "diary-photo.$extension",
                bytes.toRequestBody(type.toMediaType())
            )
            service.uploadDiaryImage(part).requireData("사진을 업로드하지 못했습니다.").key
                ?.takeIf { it.isNotBlank() } ?: error("서버가 사진 키를 반환하지 않았습니다.")
        }
    }

    @WorkerThread
    private fun readLimitedImage(uri: Uri): ByteArray {
        val input = context.contentResolver.openInputStream(uri) ?: error("선택한 사진을 열 수 없습니다.")
        return input.use { source ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val count = source.read(buffer)
                if (count < 0) break
                if (output.size() + count > 10 * 1024 * 1024) error("사진은 10MB 이하만 첨부할 수 있습니다.")
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
    }

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
