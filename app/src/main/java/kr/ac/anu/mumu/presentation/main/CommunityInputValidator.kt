package kr.ac.anu.mumu.presentation.main

object CommunityInputValidator {
    const val MAX_TITLE_LENGTH = 200

    fun validatePost(title: String, content: String): String? = when {
        title.isBlank() -> "제목을 입력해 주세요."
        title.length > MAX_TITLE_LENGTH -> "제목은 ${MAX_TITLE_LENGTH}자 이하로 입력해 주세요."
        content.isBlank() -> "내용을 입력해 주세요."
        else -> null
    }

    fun validateComment(content: String): String? = when {
        content.isBlank() -> "댓글 내용을 입력해 주세요."
        else -> null
    }
}
