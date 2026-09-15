package kr.ac.anu.mumu.presentation.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CommunityInputValidatorTest {

    @Test
    fun `게시글 제목과 내용이 비어 있으면 오류를 반환한다`() {
        assertEquals("제목을 입력해 주세요.", CommunityInputValidator.validatePost(" ", "내용"))
        assertEquals("내용을 입력해 주세요.", CommunityInputValidator.validatePost("제목", " "))
    }

    @Test
    fun `게시글 제목은 200자까지 허용한다`() {
        assertNull(
            CommunityInputValidator.validatePost(
                "가".repeat(CommunityInputValidator.MAX_TITLE_LENGTH),
                "내용"
            )
        )
        assertEquals(
            "제목은 200자 이하로 입력해 주세요.",
            CommunityInputValidator.validatePost(
                "가".repeat(CommunityInputValidator.MAX_TITLE_LENGTH + 1),
                "내용"
            )
        )
    }

    @Test
    fun `댓글은 공백만 입력할 수 없다`() {
        assertEquals("댓글 내용을 입력해 주세요.", CommunityInputValidator.validateComment("  "))
        assertNull(CommunityInputValidator.validateComment("좋아요"))
    }
}
