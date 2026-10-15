package kr.ac.anu.mumu.presentation.analysis

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kr.ac.anu.mumu.domain.model.BehaviorAnalysisResult
import kr.ac.anu.mumu.domain.repository.AnalysisRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun returningToLoadingDoesNotRestartPendingOrCompletedRequest() = runTest(dispatcher) {
        var calls = 0
        val response = CompletableDeferred<Result<BehaviorAnalysisResult>>()
        val viewModel = AnalysisViewModel(
            repository {
                calls++
                response.await()
            }
        )

        viewModel.analyze("video", "request", demo = false)
        runCurrent()
        viewModel.analyze("video", "request", demo = false)
        assertEquals(1, calls)
        response.complete(Result.success(result("video")))
        runCurrent()

        viewModel.analyze("video", "request", demo = false)
        runCurrent()
        assertEquals(1, calls)
        assertTrue(viewModel.uiState.value is AnalysisUiState.Success)
    }

    @Test
    fun explicitNewAnalysisOfSameVideoStartsNewRequest() = runTest(dispatcher) {
        var calls = 0
        val viewModel = AnalysisViewModel(
            repository {
                calls++
                Result.success(result(it))
            }
        )
        viewModel.analyze("video", "first", demo = false)
        runCurrent()
        viewModel.analyze("video", "second", demo = false)
        assertTrue(viewModel.uiState.value is AnalysisUiState.Loading)
        runCurrent()
        assertEquals(2, calls)
    }

    @Test
    fun olderRequestCannotOverwriteNewerResultEvenIfCancellationIsSwallowed() = runTest(dispatcher) {
        val oldResponse = CompletableDeferred<Result<BehaviorAnalysisResult>>()
        val viewModel = AnalysisViewModel(
            repository { uri ->
                if (uri == "old") {
                    withContext(NonCancellable) { oldResponse.await() }
                } else {
                    Result.success(result(uri))
                }
            }
        )
        viewModel.analyze("old", "first", demo = false)
        runCurrent()
        viewModel.analyze("new", "second", demo = false)
        runCurrent()
        oldResponse.complete(Result.success(result("old")))
        runCurrent()
        assertEquals("new", (viewModel.uiState.value as AnalysisUiState.Success).result.videoUri)
    }

    @Test
    fun failedRequestOnlyRestartsOnExplicitRetry() = runTest(dispatcher) {
        var calls = 0
        val viewModel = AnalysisViewModel(
            repository {
                calls++
                Result.failure(IllegalStateException("failed"))
            }
        )
        viewModel.analyze("video", "request", demo = false)
        runCurrent()
        viewModel.analyze("video", "request", demo = false)
        runCurrent()
        assertEquals(1, calls)
        viewModel.analyze("video", "request", retry = true, demo = false)
        runCurrent()
        assertEquals(2, calls)
    }

    @Test
    fun demoContinuesWithoutLoadingViewAndDoesNotRestartOnReturn() = runTest(dispatcher) {
        val viewModel = AnalysisViewModel(repository { error("Demo must not call server") })
        viewModel.analyze("video", "request", demo = true)
        runCurrent()
        advanceTimeBy(4_000)
        viewModel.analyze("video", "request", demo = true)
        advanceTimeBy(5_000)
        runCurrent()
        assertTrue(viewModel.uiState.value is AnalysisUiState.Success)
    }

    private fun repository(block: suspend (String) -> Result<BehaviorAnalysisResult>) =
        object : AnalysisRepository {
            override suspend fun analyzeBehavior(videoUri: String) = block(videoUri)
        }

    private fun result(uri: String) = BehaviorAnalysisResult(uri, true, 90, "정상", "이유", "추천")
}
