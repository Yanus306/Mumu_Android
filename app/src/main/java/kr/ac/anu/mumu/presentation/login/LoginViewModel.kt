package kr.ac.anu.mumu.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kr.ac.anu.mumu.domain.usecase.LoginUseCase
import javax.inject.Inject

@HiltViewModel
// 화면 상태
sealed class LoginUiState {
    object Idle : LoginUiState() // 대기
    object Loading : LoginUiState() // 로딩
    object Success : LoginUiState() // 로그인 성공
    data class Error(val message: String) : LoginUiState() // 실패
}

class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // 로그인 함수
    fun login(id: String, pw: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            loginUseCase(id, pw)
                .onSuccess {
                    _uiState.value = LoginUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = LoginUiState.Error(error.message ?: "로그인 실패")
                }

        }
    }
}