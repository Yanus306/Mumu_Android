package kr.ac.anu.mumu.presentation.main

import kr.ac.anu.mumu.databinding.DialogDiaryFormBinding

fun DialogDiaryFormBinding.fitContentHeight(height: Int) {
    val density = root.resources.displayMetrics.density
    val target = (height - 342 * density).toInt().coerceAtLeast((200 * density).toInt())
    if (etDiaryContent.layoutParams.height != target) {
        etDiaryContent.layoutParams = etDiaryContent.layoutParams.apply { this.height = target }
    }
}
