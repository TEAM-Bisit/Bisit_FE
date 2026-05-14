package kr.bisit.app.ui.shop

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.graphics.toColorInt
import kr.bisit.app.R
import kotlin.math.max

class HighlightOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    enum class HighlightShape {
        RECT,
        ROUNDED_RECT,
        CIRCLE
    }

    data class HighlightSpec(
        val rect: RectF,
        val shape: HighlightShape,
        val radiusPx: Float = 0f
    )

    private var mixedSpecs: List<HighlightSpec> = emptyList()

    private val dimPaint = Paint().apply {
        color = "#CC222222".toColorInt()
        style = Paint.Style.FILL
    }

    private val clearPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private var singleRect: RectF? = null
    private var multipleRects: List<RectF> = emptyList()

    private var highlightShape = HighlightShape.ROUNDED_RECT

    private var cornerRadius = 0f

    private var skipClickListener: (() -> Unit)? = null
    private var nextClickListener: (() -> Unit)? = null

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_SOFTWARE, null)

        isClickable = true
        isFocusable = true

        LayoutInflater.from(context)
            .inflate(R.layout.view_overlay_skip, this, true)

        findViewById<View>(R.id.btnSkip).setOnClickListener {
            skipClickListener?.invoke()
        }

        findViewById<View>(R.id.btnNext).setOnClickListener {
            nextClickListener?.invoke()
        }
    }

    /* ================= 버튼 리스너 ================= */

    fun setOnSkipClickListener(listener: () -> Unit) {
        skipClickListener = listener
    }

    fun setOnNextClickListener(listener: () -> Unit) {
        nextClickListener = listener
    }

    /* ================= 단일 Highlight ================= */

    fun highlight(
        rect: RectF,
        shape: HighlightShape = HighlightShape.ROUNDED_RECT,
        radiusDp: Float = 16f
    ) {
        mixedSpecs = emptyList()
        multipleRects = emptyList()
        highlightShape = shape
        cornerRadius = dpToPx(radiusDp)

        val padding = dpToPx(12f)
        singleRect = createRect(rect, shape, padding)

        invalidate()
    }

    /* ================= 다중 Highlight ================= */

    fun highlightMultiple(
        rects: List<RectF>,
        shape: HighlightShape = HighlightShape.CIRCLE,
        radiusDp: Float = 12f
    ) {
        mixedSpecs = emptyList()
        singleRect = null
        highlightShape = shape
        cornerRadius = dpToPx(radiusDp)

        val padding = dpToPx(10f)

        multipleRects = rects.map { rect ->
            createRect(rect, shape, padding)
        }

        invalidate()
    }

    fun highlightMixed(specs: List<HighlightSpec>) {
        singleRect = null
        multipleRects = emptyList()

        mixedSpecs = specs.map { spec ->
            val padding = dpToPx(10f)
            val r = createRect(spec.rect, spec.shape, padding)
            spec.copy(rect = r) // rect에 padding 적용
        }

        invalidate()
    }

    /* ================= Rect 생성 ================= */

    private fun createRect(
        rect: RectF,
        shape: HighlightShape,
        padding: Float
    ): RectF {

        return when (shape) {

            HighlightShape.CIRCLE -> {
                val minSize = dpToPx(72f)
                val size = max(max(rect.width(), rect.height()), minSize)

                val cx = rect.centerX()
                val cy = rect.centerY()

                RectF(
                    cx - size / 2f,
                    cy - size / 2f,
                    cx + size / 2f,
                    cy + size / 2f
                )
            }

            else -> {
                RectF(
                    rect.left - padding,
                    rect.top - padding,
                    rect.right + padding,
                    rect.bottom + padding
                )
            }
        }
    }

    fun clearHighlight() {
        singleRect = null
        multipleRects = emptyList()
        mixedSpecs = emptyList()
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = true

    /* ================= 핵심 Draw ================= */

    override fun dispatchDraw(canvas: Canvas) {

        val save = canvas.saveLayer(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            null
        )

        canvas.drawRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            dimPaint
        )

        singleRect?.let { drawHole(canvas, it) }
        multipleRects.forEach { drawHole(canvas, it) }

        mixedSpecs.forEach { spec ->
            drawHole(canvas, spec.rect, spec.shape, spec.radiusPx)
        }

        canvas.restoreToCount(save)

        super.dispatchDraw(canvas)
    }

    private fun drawHole(canvas: Canvas, rect: RectF) {

        when (highlightShape) {
            HighlightShape.RECT ->
                canvas.drawRect(rect, clearPaint)

            HighlightShape.ROUNDED_RECT ->
                canvas.drawRoundRect(rect, cornerRadius, cornerRadius, clearPaint)

            HighlightShape.CIRCLE ->
                canvas.drawOval(rect, clearPaint)
        }
    }

    private fun drawHole(
        canvas: Canvas,
        rect: RectF,
        shape: HighlightShape,
        radiusPx: Float
    ) {
        when (shape) {
            HighlightShape.RECT ->
                canvas.drawRect(rect, clearPaint)

            HighlightShape.ROUNDED_RECT ->
                canvas.drawRoundRect(rect, radiusPx, radiusPx, clearPaint)

            HighlightShape.CIRCLE ->
                canvas.drawOval(rect, clearPaint)
        }
    }

    private fun dpToPx(dp: Float): Float =
        dp * resources.displayMetrics.density
}
