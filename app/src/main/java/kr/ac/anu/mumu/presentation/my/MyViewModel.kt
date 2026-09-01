package kr.ac.anu.mumu.presentation.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.model.MyInformation
import kr.ac.anu.mumu.domain.usecase.MyUseCase
import javax.inject.Inject

sealed class MyUiState{
    object Init: MyUiState()
    object Loading: MyUiState()
    data class Success(val myInfo: MyInformation): MyUiState()
    data class Error(val message: String): MyUiState()
}
@HiltViewModel
class MyViewModel @Inject constructor(
    private var myUseCase: MyUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Init)
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()

    fun getUserProfile(userId: Int) {
        viewModelScope.launch {
            _uiState.value = MyUiState.Loading
            val result = myUseCase(userId)
            result.onSuccess { myInfo ->
                _uiState.value = MyUiState.Success(myInfo)
            }.onFailure { error ->
                _uiState.value = MyUiState.Error(error.message ?: "프로필을 불러오는데 실패했습니다.")
            }
        }
    }
}