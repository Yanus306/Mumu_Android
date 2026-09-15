package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.data.model.HospitalDetailDto
import kr.ac.anu.mumu.data.model.HospitalListDto
import kr.ac.anu.mumu.data.model.HospitalPriceDto
import kr.ac.anu.mumu.data.model.HospitalReviewDto
import kr.ac.anu.mumu.domain.repository.HospitalRepository
import javax.inject.Inject

sealed interface HospitalUiState {
    data object Loading : HospitalUiState
    data class Ready(val hospitals: List<HospitalListDto>, val keyword: String, val nextPage: Int?, val isLoadingMore: Boolean = false) : HospitalUiState
    data class Error(val message: String) : HospitalUiState
}

data class HospitalDetailContent(val hospital: HospitalDetailDto, val prices: List<HospitalPriceDto>, val reviews: List<HospitalReviewDto>)
sealed interface HospitalEvent {
    data class Open(val content: HospitalDetailContent) : HospitalEvent
    data class Message(val text: String) : HospitalEvent
}

@HiltViewModel
class HospitalViewModel @Inject constructor(private val repository: HospitalRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<HospitalUiState>(HospitalUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<HospitalEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init { search("") }

    fun search(keyword: String) = viewModelScope.launch {
        _uiState.value = HospitalUiState.Loading
        repository.search(keyword.trim().takeIf(String::isNotEmpty), 0)
            .onSuccess { page -> _uiState.value = HospitalUiState.Ready(page.content, keyword.trim(), page.nextPage()) }
            .onFailure { _uiState.value = HospitalUiState.Error(it.message ?: "병원을 찾지 못했습니다.") }
    }

    fun loadMore() {
        val current = _uiState.value as? HospitalUiState.Ready ?: return
        val number = current.nextPage ?: return
        if (current.isLoadingMore) return
        _uiState.value = current.copy(isLoadingMore = true)
        viewModelScope.launch {
            repository.search(current.keyword.takeIf(String::isNotEmpty), number)
                .onSuccess { page -> _uiState.value = current.copy(hospitals = current.hospitals + page.content, nextPage = page.nextPage()) }
                .onFailure { _uiState.value = current; _events.emit(HospitalEvent.Message(it.message ?: "더 불러오지 못했습니다.")) }
        }
    }

    fun open(id: Long) = viewModelScope.launch {
        val detail = async { repository.getDetail(id) }
        val prices = async { repository.getPrices(id) }
        val reviews = async { repository.getReviews(id) }
        detail.await().onSuccess {
            _events.emit(HospitalEvent.Open(HospitalDetailContent(it, prices.await().getOrDefault(emptyList()), reviews.await().getOrDefault(emptyList()))))
        }.onFailure {
            prices.await(); reviews.await()
            _events.emit(HospitalEvent.Message(it.message ?: "병원 정보를 불러오지 못했습니다."))
        }
    }

    private fun <T> kr.ac.anu.mumu.data.model.PaginatedData<T>.nextPage() = if (page + 1 < totalPages) page + 1 else null
}
