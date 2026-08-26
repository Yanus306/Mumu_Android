package kr.ac.anu.mumu.data.model

data class JoinRequest(
    val loginId: String,
    val password: String,
    val name: String,
    val phone: String,
    val termsAgreed: Boolean,
    val privacyAgreed: Boolean,
    val marketingAgreed: Boolean
)

data class JoinResponse(
    val success: Boolean,
    val message: String,
    val data: UserDto?
) {
    data class UserDto(
        val userId: Int,
        val loginId: String,
        val name: String,
        val phone: String
    )
}
