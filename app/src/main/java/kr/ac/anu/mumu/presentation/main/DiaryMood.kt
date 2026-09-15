package kr.ac.anu.mumu.presentation.main

import android.widget.TextView
import androidx.core.content.ContextCompat
import kr.ac.anu.mumu.R

fun TextView.bindDiaryMood(mood: String) {
    val icon = when (mood.lowercase()) {
        "happy" -> R.drawable.ic_diary_happy
        "sad" -> R.drawable.ic_diary_sad
        else -> null
    }
    text = ""
    textSize = 0f
    includeFontPadding = false
    setPadding(0, 0, 0, 0)
    contentDescription = when (mood.lowercase()) {
        "happy" -> "행복"
        "sad" -> "속상"
        else -> "보통"
    }
    val drawable = icon?.let { ContextCompat.getDrawable(context, it) } ?: NeutralMoodDrawable(resources)
    val size = (50 * resources.displayMetrics.density).toInt()
    drawable?.setBounds(0, 0, size, size)
    setCompoundDrawables(null, drawable, null, null)
}
