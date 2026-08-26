package kr.ac.anu.mumu.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kr.ac.anu.mumu.data.datasource.AnalysisService
import kr.ac.anu.mumu.data.datasource.PetService
import kr.ac.anu.mumu.data.model.AnalysisDetailDto
import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlin.math.roundToInt

class AnalysisRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analysisService: AnalysisService,
    private val petService: PetService
) : AnalysisRepository {

    override suspend fun analyzeBehavior(videoUri: String): Result<BehaviorAnalysisResult> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val petId = getPrimaryPetId()
                val uri = Uri.parse(videoUri)
                val videoPart = createVideoPart(uri)

                try {
                    val triggerResponse = analysisService.triggerBehaviorAnalysis(petId, videoPart)
                    val triggerBody = triggerResponse.body()
                    if (!triggerResponse.isSuccessful || triggerBody?.success != true) {
                        error(triggerBody?.message ?: "분석 요청에 실패했습니다. (${triggerResponse.code()})")
                    }
                    val analysisId = triggerBody.data?.analysisId
                        ?: error("서버가 분석 ID를 반환하지 않았습니다.")
                    pollResult(analysisId, videoUri)
                } finally {
                    videoPart.body.let { body ->
                        if (body is FileRequestBody) body.file.delete()
                    }
                }
            }
        }
    }

    private suspend fun getPrimaryPetId(): Long {
        val response = petService.getMyPets()
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            error(body?.message ?: "반려동물 정보를 불러오지 못했습니다. (${response.code()})")
        }
        return body.data?.firstOrNull()?.petId
            ?: error("분석할 반려동물을 먼저 등록해 주세요.")
    }

    private fun createVideoPart(uri: Uri): MultipartBody.Part {
        val metadata = readMetadata(uri)
        if (metadata.size != null && metadata.size > MAX_VIDEO_BYTES) {
            error("영상은 100MB 이하만 업로드할 수 있습니다.")
        }

        val extension = metadata.name.substringAfterLast('.', "mp4").lowercase()
        if (extension !in SUPPORTED_EXTENSIONS) {
            error("mp4, avi, mov, mkv 영상만 분석할 수 있습니다.")
        }

        val temporaryFile = File.createTempFile("mumu-behavior-", ".$extension", context.cacheDir)
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: error("선택한 영상을 열 수 없습니다.")
            input.use { source ->
                temporaryFile.outputStream().use(source::copyTo)
            }
            if (temporaryFile.length() > MAX_VIDEO_BYTES) {
                error("영상은 100MB 이하만 업로드할 수 있습니다.")
            }

            val mediaType = (context.contentResolver.getType(uri) ?: "video/$extension").toMediaTypeOrNull()
            val requestBody = FileRequestBody(mediaType, temporaryFile)
            return MultipartBody.Part.createFormData("video", metadata.name, requestBody)
        } catch (error: Throwable) {
            temporaryFile.delete()
            throw error
        }
    }

    private fun readMetadata(uri: Uri): VideoMetadata {
        val projection = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
                val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
                return VideoMetadata(name ?: "behavior-video.mp4", size)
            }
        }
        return VideoMetadata("behavior-video.mp4", null)
    }

    private suspend fun pollResult(analysisId: Long, videoUri: String): BehaviorAnalysisResult {
        repeat(MAX_POLL_ATTEMPTS) {
            val response = analysisService.getBehaviorAnalysis(analysisId)
            val body = response.body()
            if (!response.isSuccessful || body?.success != true) {
                error(body?.message ?: "분석 결과를 불러오지 못했습니다. (${response.code()})")
            }

            val detail = body.data ?: error("분석 결과가 비어 있습니다.")
            when (detail.status.uppercase()) {
                "COMPLETED" -> return detail.toResult(videoUri)
                "FAILED" -> error("영상 분석에 실패했습니다. 다른 영상으로 다시 시도해 주세요.")
            }
            delay(POLL_INTERVAL_MILLIS)
        }
        error("분석 시간이 오래 걸리고 있습니다. 잠시 후 내역에서 결과를 확인해 주세요.")
    }

    private fun AnalysisDetailDto.toResult(videoUri: String): BehaviorAnalysisResult {
        val probability = confidence?.roundToInt()?.coerceIn(0, 100) ?: 0
        val items = suspectedItems.orEmpty().filter(String::isNotBlank)
        val label = resultLabel.orEmpty()
        val isNormal = label.equals("NORMAL", ignoreCase = true) ||
            label == "정상" ||
            (items.isEmpty() && probability < NORMAL_THRESHOLD)
        val behavior = if (isNormal) {
            "특이 행동 없음"
        } else {
            items.joinToString(", ").ifBlank { label.ifBlank { "이상 행동 의심" } }
        }
        val reason = if (isNormal) {
            "분석 영상에서 뚜렷한 이상 행동이 감지되지 않았습니다."
        } else {
            "AI 분석에서 $behavior 패턴이 감지되었습니다."
        }
        val recommendation = if (isNormal) {
            "현재 상태를 계속 관찰하고 새로운 이상 행동이 보이면 다시 분석해 주세요."
        } else {
            "같은 행동이 반복되면 가까운 동물병원에서 정밀 검사를 받아보세요."
        }

        return BehaviorAnalysisResult(
            videoUri = videoUri,
            isNormal = isNormal,
            probability = probability,
            suspectedBehavior = behavior,
            reason = reason,
            recommendation = recommendation
        )
    }

    private class FileRequestBody(
        contentType: MediaType?,
        val file: File
    ) : RequestBody() {
        private val delegate = file.asRequestBody(contentType)

        override fun contentType(): MediaType? = delegate.contentType()
        override fun contentLength(): Long = delegate.contentLength()
        override fun writeTo(sink: okio.BufferedSink) = delegate.writeTo(sink)
    }

    private data class VideoMetadata(val name: String, val size: Long?)

    private companion object {
        const val MAX_VIDEO_BYTES = 100L * 1024L * 1024L
        const val MAX_POLL_ATTEMPTS = 60
        const val POLL_INTERVAL_MILLIS = 5_000L
        const val NORMAL_THRESHOLD = 50
        val SUPPORTED_EXTENSIONS = setOf("mp4", "avi", "mov", "mkv")
    }
}
