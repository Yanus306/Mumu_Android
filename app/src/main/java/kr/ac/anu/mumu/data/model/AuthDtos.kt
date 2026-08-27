package kr.ac.anu.mumu.data.model

data class LoginRequest(
    val loginId: String,
    val password: String,
    val deviceType: String = "android",
    val deviceInfo: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: TokenDto
)

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
