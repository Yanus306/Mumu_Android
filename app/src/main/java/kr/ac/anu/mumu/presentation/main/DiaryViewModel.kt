package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.data.local.SessionManager
import kr.ac.anu.mumu.data.model.DiaryDetailDto
import kr.ac.anu.mumu.data.model.DiaryListDto
import kr.ac.anu.mumu.data.model.DiaryRequestDto
import kr.ac.anu.mumu.data.model.selectedPetId
import kr.ac.anu.mumu.domain.repository.DiaryRepository
import kr.ac.anu.mumu.domain.repository.PetRepository
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

sealed interface DiaryUiState {
    data object Loading : DiaryUiState
    data class Ready(
        val petId: Long?,
        val petName: String?,
        val entries: List<DiaryListDto>,
        val nextPage: Int?,
        val calendarMonth: YearMonth = YearMonth.now(),
        val writtenDates: Set<LocalDate> = emptySet(),
        val selectedDate: LocalDate? = null,
        val isCalendarLoading: Boolean = false,
        val calendarError: String? = null,
        val isWorking: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : DiaryUiState
    data class Error(val message: String) : DiaryUiState
}

sealed interface DiaryEvent {
    data class Open(val diary: DiaryDetailDto) : DiaryEvent
    data object Saved : DiaryEvent
    data class Message(val text: String) : DiaryEvent
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository,
    private val petRepository: PetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<DiaryUiState>(DiaryUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<DiaryEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            val previous = _uiState.value as? DiaryUiState.Ready
            _uiState.value = DiaryUiState.Loading
            petRepository.getPets()
                .onSuccess { pets ->
                    val petId = pets.selectedPetId(sessionManager.selectedPetId)
                    val petName = pets.firstOrNull { it.petId == petId }?.name
                    if (petId == null) {
                        _uiState.value = DiaryUiState.Ready(null, null, emptyList(), null)
                    } else {
                        val samePet = previous?.takeIf { it.petId == petId }
                        val month = samePet?.calendarMonth ?: YearMonth.now()
                        val selected = samePet?.selectedDate
                        repository.getDiaries(petId, 0)
                            .onSuccess { page ->
                                _uiState.value = DiaryUiState.Ready(
                                    petId,
                                    petName,
                                    page.content,
                                    if (page.page + 1 < page.totalPages) page.page + 1 else null,
                                    calendarMonth = month,
                                    selectedDate = selected,
                                    isCalendarLoading = true
                                )
                                loadCalendar(petId, month)
                            }
                            .onFailure {
                                _uiState.value = DiaryUiState.Error(it.message ?: "일기를 불러오지 못했습니다.")
                            }
                    }
                }
                .onFailure {
                    _uiState.value = DiaryUiState.Error(it.message ?: "반려동물을 불러오지 못했습니다.")
                }
        }
    }

    fun changeMonth(offset: Long) {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        val petId = current.petId ?: return
        val month = current.calendarMonth.plusMonths(offset)
        _uiState.value = current.copy(
            calendarMonth = month,
            writtenDates = emptySet(),
            selectedDate = null,
            isCalendarLoading = true,
            calendarError = null
        )
        viewModelScope.launch { loadCalendar(petId, month) }
    }

    fun selectDate(date: LocalDate) {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        if (current.petId == null || YearMonth.from(date) != current.calendarMonth || date.isAfter(LocalDate.now())) return
        _uiState.value = current.copy(selectedDate = date)
    }

    fun retryCalendar() {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        val petId = current.petId ?: return
        if (current.isCalendarLoading) return
        _uiState.value = current.copy(isCalendarLoading = true, calendarError = null)
        viewModelScope.launch { loadCalendar(petId, current.calendarMonth) }
    }

    private suspend fun loadCalendar(petId: Long, month: YearMonth) {
        repository.getCalendar(petId, month.year, month.monthValue)
            .onSuccess { response ->
                val current = _uiState.value as? DiaryUiState.Ready ?: return@onSuccess
                if (current.petId != petId || current.calendarMonth != month) return@onSuccess
                _uiState.value = current.copy(
                    writtenDates = response.writtenDates.mapNotNull { runCatching { LocalDate.parse(it) }.getOrNull() }.toSet(),
                    isCalendarLoading = false,
                    calendarError = null
                )
            }
            .onFailure { error ->
                val current = _uiState.value as? DiaryUiState.Ready ?: return@onFailure
                if (current.petId != petId || current.calendarMonth != month) return@onFailure
                _uiState.value = current.copy(
                    isCalendarLoading = false,
                    calendarError = error.message ?: "달력을 불러오지 못했습니다."
                )
            }
    }

    fun loadMore() {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        val petId = current.petId ?: return
        val page = current.nextPage ?: return
        if (current.isWorking || current.isLoadingMore) return
        viewModelScope.launch {
            _uiState.value = current.copy(isLoadingMore = true)
            repository.getDiaries(petId, page)
                .onSuccess { result ->
                    _uiState.value = current.copy(
                        entries = (current.entries + result.content).distinctBy { it.diaryId },
                        nextPage = if (result.page + 1 < result.totalPages) result.page + 1 else null
                    )
                }
                .onFailure {
                    _uiState.value = current
                    _events.emit(DiaryEvent.Message(it.message ?: "다음 일기를 불러오지 못했습니다."))
                }
        }
    }

    fun openDiary(diaryId: Long) {
        viewModelScope.launch {
            repository.getDiary(diaryId)
                .onSuccess { _events.emit(DiaryEvent.Open(it)) }
                .onFailure { _events.emit(DiaryEvent.Message(it.message ?: "일기를 열지 못했습니다.")) }
        }
    }

    fun saveDiary(diaryId: Long?, request: DiaryRequestDto) {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        if (current.isWorking || current.petId != request.petId) return
        viewModelScope.launch {
            _uiState.value = current.copy(isWorking = true)
            repository.saveDiary(diaryId, request)
                .onSuccess {
                    _events.emit(DiaryEvent.Saved)
                    load()
                }
                .onFailure {
                    _uiState.value = current
                    _events.emit(DiaryEvent.Message(it.message ?: "일기를 저장하지 못했습니다."))
                }
        }
    }

    fun deleteDiary(diaryId: Long) {
        val current = _uiState.value as? DiaryUiState.Ready ?: return
        if (current.isWorking) return
        viewModelScope.launch {
            _uiState.value = current.copy(isWorking = true)
            repository.deleteDiary(diaryId)
                .onSuccess {
                    _uiState.value = current.copy(entries = current.entries.filterNot { it.diaryId == diaryId })
                    _events.emit(DiaryEvent.Message("일기를 삭제했습니다."))
                }
                .onFailure {
                    _uiState.value = current
                    _events.emit(DiaryEvent.Message(it.message ?: "일기를 삭제하지 못했습니다."))
                }
        }
    }
}
