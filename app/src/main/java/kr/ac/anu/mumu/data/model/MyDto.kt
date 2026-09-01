package kr.ac.anu.mumu.data.model

import com.google.gson.annotations.SerializedName

data class MyResponse(
    val success: Boolean,
    val message: String,
    val user: MyDto
)

data class MyDto(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("login_id")
    val loginId: String,
    val name: String,
    val phone: String
)