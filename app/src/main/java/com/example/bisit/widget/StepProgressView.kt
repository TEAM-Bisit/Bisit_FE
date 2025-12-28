package com.example.bisit.widget

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.bisit.R
import kotlin.math.max

class StepProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var stepCount: Int = 4
    private var currentStep: Int = 0
    private var labels: List<String> = emptyList()

    private var activeColor = ContextCompat.getColor(context, R.color.sp_active_orange)
    private var inactiveColor = ContextCompat.getColor(context, R.color.sp_inactive)
    private var textActiveColor = ContextCompat.getColor(context, R.color.sp_active)
    private var textInactiveColor = ContextCompat.getColor(context, R.color.sp_text_inactive)

    private var circleRadiusPx = dpToPx(5f)
    private var circleOuterRadiusPx = dpToPx(8f)
    private var lineStrokePx = dpToPx(2f)
    private var textSizePx = spToPx(10f)
    private var textMarginPx = dpToPx(6f)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.StepProgressView)
            stepCount = a.getInt(R.styleable.StepProgressView_stepCount, stepCount)
            currentStep = a.getInt(R.styleable.StepProgressView_currentStep, currentStep)
            activeColor = a.getColor(R.styleable.StepProgressView_activeColor, activeColor)
            inactiveColor = a.getColor(R.styleable.StepProgressView_inactiveColor, inactiveColor)
            circleRadiusPx = a.getDimension(R.styleable.StepProgressView_circleRadius, circleRadiusPx)
            circleOuterRadiusPx = a.getDimension(R.styleable.StepProgressView_outerCircleRadius, circleOuterRadiusPx)
            lineStrokePx = a.getDimension(R.styleable.StepProgressView_lineStroke, lineStrokePx)
            textSizePx = a.getDimension(R.styleable.StepProgressView_textSize, textSizePx)
            a.recycle()
        }

        linePaint.style = Paint.Style.STROKE
        linePaint.strokeWidth = lineStrokePx
        linePaint.strokeCap = Paint.Cap.ROUND

        circlePaint.style = Paint.Style.FILL
        innerCirclePaint.style = Paint.Style.FILL
        innerCirclePaint.color = ContextCompat.getColor(context, R.color.white)

        textPaint.textSize = textSizePx
        textPaint.textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Calculate required horizontal padding for text
        var maxTextWidth = 0f
        if (labels.isNotEmpty()) {
            // Check first and last labels specifically as they are most prone to clipping
            val firstWidth = textPaint.measureText(labels.first())
            val lastWidth = textPaint.measureText(labels.last())
            maxTextWidth = max(firstWidth, lastWidth)
        }

        // Ensure enough space for the text at the edges
        // We need at least half the text width at the start and end
        val minSidePadding = (maxTextWidth / 2).toInt() + dpToPx(8f).toInt()

        // Update padding if needed (respecting original padding if it's larger)
        val safePaddingLeft = max(paddingLeft, minSidePadding)
        val safePaddingRight = max(paddingRight, minSidePadding)

        // We don't actually change the view's padding property, but we'll use these values in onDraw
        // However, for onMeasure, we just need to ensure height is correct
        val desiredHeight = (paddingTop + paddingBottom + circleOuterRadiusPx * 2 + textMarginPx + textSizePx * 1.5f).toInt()

        val h = resolveSize(desiredHeight, heightMeasureSpec)
        val w = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        if (stepCount <= 1) return

        // Calculate safe drawing area to avoid text clipping
        val firstLabelWidth = if (labels.isNotEmpty()) textPaint.measureText(labels[0]) else 0f
        val lastLabelWidth = if (labels.isNotEmpty()) textPaint.measureText(labels[labels.size - 1]) else 0f

        val sideMargin = max(circleOuterRadiusPx, max(firstLabelWidth, lastLabelWidth) / 2f)

        val drawStart = paddingLeft + sideMargin
        val drawEnd = width - paddingRight - sideMargin
        val availableW = drawEnd - drawStart

        val spacing = if (stepCount > 1) availableW / (stepCount - 1) else 0f
        val centerY = paddingTop + circleOuterRadiusPx

        // inactive full line
        linePaint.color = inactiveColor
        canvas.drawLine(drawStart, centerY, drawEnd, centerY, linePaint)

        // active segments
        linePaint.color = activeColor
        for (i in 0 until stepCount - 1) {
            val startX = drawStart + i * spacing
            val endX = drawStart + (i + 1) * spacing
            if (i < currentStep) {
                canvas.drawLine(startX, centerY, endX, centerY, linePaint)
            }
        }

        // circles and labels
        textPaint.textSize = textSizePx
        for (i in 0 until stepCount) {
            val cx = drawStart + i * spacing

            when {
                i < currentStep -> {
                    circlePaint.color = activeColor
                    canvas.drawCircle(cx, centerY, circleRadiusPx, circlePaint)
                }
                i == currentStep -> {
                    // outer colored ring + inner white circle
                    circlePaint.color = activeColor
                    canvas.drawCircle(cx, centerY, circleOuterRadiusPx, circlePaint)
                    canvas.drawCircle(cx, centerY, circleRadiusPx, innerCirclePaint)
                }
                else -> {
                    circlePaint.color = inactiveColor
                    canvas.drawCircle(cx, centerY, circleRadiusPx, circlePaint)
                }
            }

            val label = if (i < labels.size) labels[i] else ""
            if (i == currentStep) {
                textPaint.color = textActiveColor
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                textPaint.color = textInactiveColor
                textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val textY = centerY + circleOuterRadiusPx + textMarginPx + (textSizePx / 2f)
            canvas.drawText(label, cx, textY, textPaint)
        }
    }

    fun setStepCount(count: Int) {
        stepCount = max(1, count)
        invalidate()
        requestLayout()
    }

    fun setCurrentStep(stepZeroBased: Int) {
        currentStep = stepZeroBased.coerceIn(0, stepCount - 1)
        invalidate()
    }

    fun setLabels(list: List<String>) {
        labels = list
        invalidate()
        requestLayout()
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
}