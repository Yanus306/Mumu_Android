package kr.ac.anu.mumu.data.model

import com.google.gson.annotations.SerializedName

data class JoinRequest(
    val id: String,
    val password: String,
    val name: String,
    val phone: String,
    val address: String,
    @SerializedName("detail_address") val detailAddress: String,
    @SerializedName("postal_code") val postalCode: String,
    @SerializedName("terms_agreed") val termsAgreed: Boolean,
    @SerializedName("privacy_agreed") val privacyAgreed: Boolean,
    @SerializedName("marketing_agreed") val marketingAgreed: Boolean
)

data class JoinResponse(
    val success: Boolean,
    val message: String,
    val token: String?,
    val user: UserDto?
) {
    data class UserDto(
        val userId: Int,
        val id: String,
        val name: String,
        val phone: String
    )
}
