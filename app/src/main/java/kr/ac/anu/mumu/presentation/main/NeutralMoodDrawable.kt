package kr.ac.anu.mumu.presentation.main

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import kr.ac.anu.mumu.R

/** Displays the supplied design crop without changing its face geometry. */
class NeutralMoodDrawable(resources: Resources) : Drawable() {
    private val bitmap = BitmapFactory.decodeResource(
        resources,
        R.drawable.diary_normal_source,
        BitmapFactory.Options().apply { inScaled = false }
    )
    private val source = Rect(55, 11, 219, 175)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

    override fun draw(canvas: Canvas) = canvas.drawBitmap(bitmap, source, bounds, paint)

    override fun setAlpha(alpha: Int) { paint.alpha = alpha; invalidateSelf() }

    override fun setColorFilter(colorFilter: ColorFilter?) { paint.colorFilter = colorFilter; invalidateSelf() }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
