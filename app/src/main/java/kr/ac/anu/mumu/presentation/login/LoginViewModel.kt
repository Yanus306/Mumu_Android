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
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // 화면 상태
    sealed class UiState {
        object Idle : UiState() // 대기
        object Loading : UiState() // 로딩
        object Success : UiState() // 로그인 성공
        data class Error(val message: String) : UiState() // 실패
    }

    private val _loginState = MutableStateFlow<UiState>(UiState.Idle)
    val loginState: StateFlow<UiState> = _loginState.asStateFlow()

    // 로그인 함수
    fun login(id: String, pw: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            loginUseCase(id, pw)
                .onSuccess {
                    _loginState.value = UiState.Success
                }
                .onFailure { error ->
                    _loginState.value = UiState.Error(error.message ?: "로그인 실패")
                }

        }
    }
}