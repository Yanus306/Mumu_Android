package kr.ac.anu.mumu.presentation.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class JoinViewModel @Inject constructor() : ViewModel() {

    // 입력 데이터
    val inputId = MutableLiveData("")
    val inputPw = MutableLiveData("")
    val inputPwCheck = MutableLiveData("")
    val inputName = MutableLiveData("")
    val inputPhoneNum = MutableLiveData("")
    val inputCheckNum = MutableLiveData("")
    val inputPostalCode = MutableLiveData("")
    val inputAddress = MutableLiveData("")
    val inputDetailAddress = MutableLiveData("")
    val isAllAgreed = MutableLiveData(false)
    val isTermsAgreed = MutableLiveData(false)
    val isPrivacyAgreed = MutableLiveData(false)
    val isMarketingAgreed = MutableLiveData(false)

    // 화면 상태
    private val _accountStep = MutableLiveData(0)
    val accountStep: LiveData<Int> get() = _accountStep

    // 에러 메시지 표시 여부
    val isIdErrorVisible = MutableLiveData(false)
    val isPwErrorVisible = MutableLiveData(false)
    val isPwCheckErrorVisible = MutableLiveData(false)
    val isCheckNumErrorVisible = MutableLiveData(false)

    // 페이지 이동 이벤트
    private val _moveToNextPage = MutableLiveData<Boolean>()
    val moveToNextPage: LiveData<Boolean> get() = _moveToNextPage

    private val _finishJoinFlow = MutableLiveData<Boolean>()
    val finishJoinFlow: LiveData<Boolean> get() = _finishJoinFlow

    // 버튼 활성화 여부
    private val _isButtonEnabled = MutableLiveData(false)
    val isButtonEnabled: LiveData<Boolean> get() = _isButtonEnabled

    // 정규식
    private val ID_REGEX = Regex("^[a-zA-Z0-9]{7,12}\$")
    private val PW_REGEX = Regex("^(?=.*[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,16}\$")

    fun checkButtonEnabled() {
        val step = _accountStep.value ?: 0
        val hasInput = when (step) {
            0 -> !inputId.value.isNullOrBlank() // ID 칸에 뭐라도 썼니?
            1 -> !inputPw.value.isNullOrBlank() // PW 칸에 뭐라도 썼니?
            2 -> !inputPwCheck.value.isNullOrBlank() // 확인 칸에 뭐라도 썼니?
            else -> false
        }
        _isButtonEnabled.value = hasInput
    }

    // 다음 버튼 클릭시 호출
    fun onNextClick() {
        when (_accountStep.value) {
            0 -> checkIdStep()
            1 -> checkPwStep()
            2 -> checkPwCheckStep()
            3 -> {
                if (!inputName.value.isNullOrBlank()) _moveToNextPage.value = true
            }
            4 -> onPhoneCheckClick()
            5 -> {
                if (!inputDetailAddress.value.isNullOrBlank()) _moveToNextPage.value = true
            }
            6 -> onTermsNextClick()
            7 -> _finishJoinFlow.value = true
        }
    }

    private fun checkIdStep() {
        val id = inputId.value ?: ""

        if (ID_REGEX.matches(id)) {
            isIdErrorVisible.value = false
            _accountStep.value = 1
            checkButtonEnabled()
        } else {
            isIdErrorVisible.value = true
        }
    }

    private fun checkPwStep() {
        val pw = inputPw.value ?: ""

        if (PW_REGEX.matches(pw)) {
            isPwErrorVisible.value = false
            _accountStep.value = 2
            checkButtonEnabled()
        } else {
            isPwErrorVisible.value = true
        }
    }

    private fun checkPwCheckStep() {
        val pw = inputPw.value ?: ""
        val pwCheck = inputPwCheck.value ?: ""

        if (pw == pwCheck && pw.isNotBlank()) {
            isPwCheckErrorVisible.value = false
            _moveToNextPage.value = true
        } else {
            isPwCheckErrorVisible.value = true
        }
    }

    // Name Code
    fun checkNameStep() {
        val hasName = !inputName.value.isNullOrBlank()
        _isButtonEnabled.value = hasName
    }

    fun onNameCheckClick() {
        if (!inputName.value.isNullOrBlank()) {
            _moveToNextPage.value = true
        }
    }

    // Phone Code
    // 인증번호 visiable 여부 결정하는 LiveData
    private val _isVerificationVisible = MutableLiveData(false)
    val isVerificationVisible: LiveData<Boolean> get() = _isVerificationVisible

    fun checkPhoneStep() {
        val phoneLength = inputPhoneNum.value?.length ?: 0
        val isPhoneValid = phoneLength == 10 || phoneLength == 11

        _isVerificationVisible.value = isPhoneValid

        checkPhoneButtonEnabled()
    }

    fun checkPhoneButtonEnabled() {
        val phoneLength = inputPhoneNum.value?.length ?: 0
        val isPhoneValid = phoneLength == 10 || phoneLength == 11

        val isCodeValid = !inputCheckNum.value.isNullOrBlank()

        // 전화번호도 맞고 인증번호도 쳤을 때만 최종 버튼 켜기
        _isButtonEnabled.value = isPhoneValid && isCodeValid
    }

    fun onPhoneCheckClick() {
        val phoneLength = inputPhoneNum.value?.length ?: 0
        val isPhoneValid = phoneLength == 10 || phoneLength == 11
        val currentCode = inputCheckNum.value ?: ""

        if (isPhoneValid) {
            if (currentCode == "123456") {
                isCheckNumErrorVisible.value = false
                _moveToNextPage.value = true
            } else {
                isCheckNumErrorVisible.value = true
            }
        }
    }

    // Address
    fun checkAddressStep() {
        //TODO 우편번호랑 도로명은 주소 API 사용하면서 작성
        val hasDetail = !inputDetailAddress.value.isNullOrBlank()
        _isButtonEnabled.value = hasDetail
    }

    fun onAddressCheckClick() {
        if (!inputDetailAddress.value.isNullOrBlank()) {
            _moveToNextPage.value = true
        }
    }

    // Terms
    fun checkAgreementStep() {
        val terms = isTermsAgreed.value ?: false
        val privacy = isPrivacyAgreed.value ?: false

        _isButtonEnabled.value = terms && privacy
    }

    fun onAllAgreeClicked(isChecked: Boolean) {
        isAllAgreed.value = isChecked
        isTermsAgreed.value = isChecked
        isPrivacyAgreed.value = isChecked
        isMarketingAgreed.value = isChecked

        checkAgreementStep()
    }

    fun onSingleAgreeClicked() {
        val terms = isTermsAgreed.value ?: false
        val privacy = isPrivacyAgreed.value ?: false
        val marketing = isMarketingAgreed.value ?: false

        isAllAgreed.value = terms && privacy && marketing

        checkAgreementStep()
    }

    fun onTermsNextClick() {
        // viewModelScop.launch { registerUserUseCase() } -> useCase 연결

        _moveToNextPage.value = true
    }

    // 네비게이션 완료 후 이벤트 초기화
    fun doneNavigation() { _moveToNextPage.value = false }
}