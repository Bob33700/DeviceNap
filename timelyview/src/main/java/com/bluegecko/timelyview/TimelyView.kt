package com.bluegecko.timelyview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.bluegecko.timelyview.animation.TimelyEvaluator
import com.bluegecko.timelyview.model.NumberUtils
import com.bluegecko.timelyview.model.number.Null
import com.nineoldandroids.animation.ObjectAnimator

@Suppress("unused")
class TimelyView : View {
    private var lastEnd = -1
    private var lastStart = -1
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var controlPoints: Array<FloatArray> = Null.controlPoints
    private var textColor = Color.BLACK
    private var isRoundedCorner = true
    private var strokeWidth = 5.0f
    private var mWidth = 1
    private var mHeight = 1

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelyView)
        textColor = typedArray.getColor(R.styleable.TimelyView_text_color, Color.BLACK)
        isRoundedCorner = typedArray.getBoolean(R.styleable.TimelyView_rounded_corner, true)
        typedArray.recycle()
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    fun setTextColor(color: Int) {
        textColor = color
        init()
    }

    fun setRoundedCorner(isRoundedCorner: Boolean) {
        this.isRoundedCorner = isRoundedCorner
        init()
    }

    fun setTextColorAndCorner(textColor: Int, isRoundedCorner: Boolean) {
        this.textColor = textColor
        this.isRoundedCorner = isRoundedCorner
        init()
    }

    fun getControlPoints(): Array<FloatArray> {
        return controlPoints
    }

    fun setControlPoints(controlPoints: Array<FloatArray>) {
        this.controlPoints = controlPoints
        invalidate()
    }

    private fun animate(start: Int, end: Int): ObjectAnimator {
        lastEnd = end
        lastStart = start
        val startPoints = NumberUtils.getControlPointsFor(start)
        val endPoints = NumberUtils.getControlPointsFor(end)
        return ObjectAnimator.ofObject(
            this,
            "controlPoints",
            TimelyEvaluator(),
            startPoints,
            endPoints
        )
    }

    private fun animate(end: Int): ObjectAnimator {
        lastEnd = end
        val startPoints = NumberUtils.getControlPointsFor(-1)
        val endPoints = NumberUtils.getControlPointsFor(end)
        return ObjectAnimator.ofObject(
            this,
            "controlPoints",
            TimelyEvaluator(),
            startPoints,
            endPoints
        )
    }

    fun animateCarefully(end: Int): ObjectAnimator? {
        return if (lastEnd == end) null else animate(end)
    }

    fun animateCarefully(start: Int, end: Int): ObjectAnimator? {
        if (lastEnd == -1 || lastStart == -1) {
            lastEnd = end
            lastStart = start
        } else if (lastEnd == end && lastStart == start) {
            return null
        }
        return animate(start, end)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (controlPoints.contentEquals(Null.controlPoints)) return
        val length = controlPoints.size
        val minDimen = if (mHeight > mWidth) mWidth - strokeWidth else mHeight - strokeWidth
        mPath!!.reset()
        mPath!!.moveTo(minDimen * controlPoints[0][0], minDimen * controlPoints[0][1])
        var i = 1
        while (i < length) {
            mPath!!.cubicTo(
                minDimen * controlPoints[i][0], minDimen * controlPoints[i][1],
                minDimen * controlPoints[i + 1][0], minDimen * controlPoints[i + 1][1],
                minDimen * controlPoints[i + 2][0], minDimen * controlPoints[i + 2][1]
            )
            i += 3
        }
        canvas.drawPath(mPath!!, mPaint!!)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
        val widthWithoutPadding = mWidth - paddingLeft - paddingRight
        val heigthWithoutPadding = mHeight - paddingTop - paddingBottom
        val maxWidth = (heigthWithoutPadding * RATIO).toInt()
        val maxHeight = (widthWithoutPadding / RATIO).toInt()
        if (widthWithoutPadding > maxWidth) {
            mWidth = maxWidth + paddingLeft + paddingRight + strokeWidth.toInt()
        } else {
            mHeight = maxHeight + paddingTop + paddingBottom + strokeWidth.toInt()
        }
        setMeasuredDimension(mWidth, mHeight)
    }

    fun setStrokeWidth(strokeWidth: Float) {
        this.strokeWidth = strokeWidth
        init()
    }

    private fun init() {
        // A new paint with the style as stroke.
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = textColor
        mPaint!!.strokeWidth = strokeWidth
        mPaint!!.style = Paint.Style.STROKE
        if (isRoundedCorner) {
            mPaint!!.strokeJoin = Paint.Join.ROUND
            mPaint!!.strokeCap = Paint.Cap.ROUND
        }
        mPath = Path()
    }

    companion object {
        private const val RATIO = 1f // square

    }
}