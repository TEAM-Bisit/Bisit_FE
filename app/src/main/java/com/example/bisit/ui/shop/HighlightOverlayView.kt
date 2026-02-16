package com.example.bisit.ui.shop

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.graphics.toColorInt
import com.example.bisit.R

class HighlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    enum class HighlightShape {
        RECT,
        ROUNDED_RECT,
        CIRCLE
    }

    private val dimPaint = Paint().apply {
        color = "#CC222222".toColorInt() // 80% opacity
    }

    private val clearPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var highlightRect: RectF? = null
    private var highlightShape: HighlightShape = HighlightShape.ROUNDED_RECT
    private var cornerRadius: Float = 0f

    private var skipClickListener: (() -> Unit)? = null

    init {
        setWillNotDraw(false)
        LayoutInflater.from(context).inflate(R.layout.view_overlay_skip, this, true)

        findViewById<TextView>(R.id.btnSkip).setOnClickListener {
            skipClickListener?.invoke()
        }
    }

    fun setOnSkipClickListener(listener: () -> Unit) {
        skipClickListener = listener
    }

    fun highlight(
        rect: RectF,
        shape: HighlightShape = HighlightShape.ROUNDED_RECT,
        radiusDp: Float = 0f
    ) {
        highlightRect = rect
        highlightShape = shape
        cornerRadius = dpToPx(radiusDp)
        invalidate()
    }

    fun clearHighlight() {
        highlightRect = null
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            null
        )

        // 1 전체 어둡게
        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            dimPaint
        )

        // 2 강조 영역 투명 처리
        highlightRect?.let { rect ->
            when (highlightShape) {
                HighlightShape.RECT -> {
                    canvas.drawRect(rect, clearPaint)
                }
                HighlightShape.ROUNDED_RECT -> {
                    canvas.drawRoundRect(
                        rect,
                        cornerRadius,
                        cornerRadius,
                        clearPaint
                    )
                }
                HighlightShape.CIRCLE -> {
                    canvas.drawOval(rect, clearPaint)
                }
            }
        }

        canvas.restoreToCount(save)

        super.dispatchDraw(canvas)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
