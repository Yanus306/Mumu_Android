package kr.ac.anu.mumu.domain.model

data class User(
    val name: String,
    val token: String,
    val profileImage: String?,
    val role: String
)
