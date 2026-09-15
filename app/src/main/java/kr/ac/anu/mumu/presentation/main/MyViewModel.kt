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
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.PetRequestDto
import kr.ac.anu.mumu.data.model.selectedPetId
import kr.ac.anu.mumu.domain.repository.PetRepository
import javax.inject.Inject

sealed interface MyUiState {
    data object Loading : MyUiState
    data class Ready(
        val pets: List<PetDto>,
        val selectedPetId: Long?,
        val isSaving: Boolean = false
    ) : MyUiState
    data class Error(val message: String) : MyUiState
}

sealed interface MyEvent {
    data object Saved : MyEvent
    data class Message(val text: String) : MyEvent
}

@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: PetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<MyEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    init { loadPets() }

    fun loadPets() {
        viewModelScope.launch {
            _uiState.value = MyUiState.Loading
            repository.getPets()
                .onSuccess { pets ->
                    val selected = pets.selectedPetId(sessionManager.selectedPetId)
                    sessionManager.selectPet(selected)
                    _uiState.value = MyUiState.Ready(pets, selected)
                }
                .onFailure { _uiState.value = MyUiState.Error(it.message ?: "반려동물을 불러오지 못했습니다.") }
        }
    }

    fun selectPet(petId: Long) {
        val current = _uiState.value as? MyUiState.Ready ?: return
        if (current.isSaving || current.pets.none { it.petId == petId }) return
        sessionManager.selectPet(petId)
        _uiState.value = current.copy(selectedPetId = petId)
    }

    fun savePet(petId: Long?, request: PetRequestDto) {
        val current = _uiState.value as? MyUiState.Ready ?: return
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            repository.savePet(petId, request)
                .onSuccess { savedPet ->
                    val pets = if (petId == null) {
                        current.pets + savedPet
                    } else {
                        current.pets.map { if (it.petId == savedPet.petId) savedPet else it }
                    }
                    val selected = current.selectedPetId ?: savedPet.petId
                    sessionManager.selectPet(selected)
                    _uiState.value = current.copy(
                        pets = pets.distinctBy { it.petId },
                        selectedPetId = selected,
                        isSaving = false
                    )
                    _events.emit(MyEvent.Saved)
                    loadPets()
                }
                .onFailure {
                    _uiState.value = current
                    _events.emit(MyEvent.Message(it.message ?: "저장하지 못했습니다."))
                }
        }
    }

    fun deletePet(petId: Long) {
        val current = _uiState.value as? MyUiState.Ready ?: return
        if (current.isSaving) return
        viewModelScope.launch {
            _uiState.value = current.copy(isSaving = true)
            repository.deletePet(petId)
                .onSuccess {
                    val pets = current.pets.filterNot { it.petId == petId }
                    val selected = current.selectedPetId.takeIf { it != petId } ?: pets.firstOrNull()?.petId
                    sessionManager.selectPet(selected)
                    _uiState.value = current.copy(pets = pets, selectedPetId = selected)
                    _events.emit(MyEvent.Message("반려동물을 삭제했습니다."))
                }
                .onFailure {
                    _uiState.value = current
                    _events.emit(MyEvent.Message(it.message ?: "삭제하지 못했습니다."))
                }
        }
    }
}
