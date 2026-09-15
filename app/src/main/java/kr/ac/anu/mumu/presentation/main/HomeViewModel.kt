package kr.ac.anu.mumu.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.data.local.SessionManager
import kr.ac.anu.mumu.data.model.PetDto
import kr.ac.anu.mumu.data.model.selectedPetId
import kr.ac.anu.mumu.domain.repository.PetRepository
import javax.inject.Inject

sealed interface HomePetUiState {
    data object Loading : HomePetUiState
    data class Ready(val pet: PetDto?) : HomePetUiState
    data class Error(val message: String) : HomePetUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: PetRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    private val _petState = MutableStateFlow<HomePetUiState>(HomePetUiState.Loading)
    val petState = _petState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.selectedPetIdFlow
                .collect { loadPet() }
        }
    }

    fun loadPet() {
        viewModelScope.launch {
            repository.getPets()
                .onSuccess { pets ->
                    val selectedId = pets.selectedPetId(sessionManager.selectedPetId)
                    sessionManager.selectPet(selectedId)
                    _petState.value = HomePetUiState.Ready(pets.firstOrNull { it.petId == selectedId })
                }
                .onFailure {
                    _petState.value = HomePetUiState.Error(it.message ?: "반려동물 정보를 불러오지 못했습니다.")
                }
        }
    }
}
