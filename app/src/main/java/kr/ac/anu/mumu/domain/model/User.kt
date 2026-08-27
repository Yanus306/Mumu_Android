package kr.ac.anu.mumu.domain.model

data class User(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String
)
