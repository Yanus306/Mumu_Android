package kr.ac.anu.mumu.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("id")
    val id: String,
    @SerializedName("password")
    val pw: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String,
    val user: UserDto
)

data class UserDto(
    @SerializedName("user_id")
    val userId: Int,
    val id: String,
    val name: String,
    val phone: String?,
    @SerializedName("profile_image")
    val profileImage: String?,
    val role: String
)
