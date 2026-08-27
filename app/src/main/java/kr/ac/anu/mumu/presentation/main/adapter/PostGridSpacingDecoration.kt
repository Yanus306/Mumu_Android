package kr.ac.anu.mumu.presentation.main.adapter

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PostGridSpacingDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        when (position % SPAN_COUNT) {
            0 -> outRect.set(9.dp(view), 0, 2.dp(view), rowTop(position, view))
            1 -> outRect.set(6.dp(view), 0, 5.dp(view), rowTop(position, view))
            2 -> outRect.set(2.dp(view), 0, 9.dp(view), rowTop(position, view))
        }
    }

    private fun rowTop(position: Int, view: View): Int {
        return if (position < SPAN_COUNT) 0 else 8.dp(view)
    }

    private fun Int.dp(view: View): Int {
        return (this * view.resources.displayMetrics.density).toInt()
    }

    private companion object {
        const val SPAN_COUNT = 3
    }
}
