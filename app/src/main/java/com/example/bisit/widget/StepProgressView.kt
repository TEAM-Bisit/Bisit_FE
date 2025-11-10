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

    private var circleRadiusPx = dpToPx(4f)
    private var circleOuterRadiusPx = dpToPx(8f)
    private var lineStrokePx = dpToPx(2f)
    private var textSizePx = spToPx(8f)
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
        val desiredHeight = (paddingTop + paddingBottom + circleOuterRadiusPx * 2 + textMarginPx + textSizePx * 1.2f).toInt()
        val h = resolveSize(desiredHeight, heightMeasureSpec)
        val w = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        if (stepCount <= 1) return

        val availableW = width - paddingLeft - paddingRight
        val spacing = availableW.toFloat() / (stepCount - 1).toFloat()
        val centerY = paddingTop + circleOuterRadiusPx

        // inactive full line
        linePaint.color = inactiveColor
        canvas.drawLine(paddingLeft.toFloat(), centerY, (width - paddingRight).toFloat(), centerY, linePaint)

        // active segments
        linePaint.color = activeColor
        for (i in 0 until stepCount - 1) {
            val startX = paddingLeft + i * spacing
            val endX = paddingLeft + (i + 1) * spacing
            if (i < currentStep) {
                canvas.drawLine(startX, centerY, endX, centerY, linePaint)
            }
        }

        // circles and labels
        textPaint.textSize = textSizePx
        for (i in 0 until stepCount) {
            val cx = paddingLeft + i * spacing

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