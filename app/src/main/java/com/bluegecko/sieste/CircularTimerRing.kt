package com.bluegecko.sieste

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.bluegecko.sieste.databinding.CircularSeekBarBinding
import kotlin.math.*

@Suppress("PrivateResource", "unused", "MemberVisibilityCanBePrivate")
class CircularTimerRing @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    View(context, attrs, defStyleAttr) {

    lateinit var circularTimer: CircularTimer

    var mTouchAngle = 0f
    var mStarted = false
    var mStart = 0L
    var mAngleTracker = AngleTracker(getCenter().x, getCenter().y)
    var mTimeFactor = minuteToMillis
    var mEnabled = true
    lateinit var mListener: OnRingChangeListener

    private var mGestureDetector = GestureDetector(context, GestureListener())
    interface OnRingChangeListener {
        fun onProgressChanged(ring: CircularTimerRing)
        fun onStartTrackingTouch(ring: CircularTimerRing)
        fun onStopTrackingTouch(ring: CircularTimerRing)
    }
    var mIncrement = mTimeFactor
        set(value) {
            if (value * minuteToMillis != field) {
                field = value * minuteToMillis
                mProgress = (mProgress / mIncrement) * mIncrement
                invalidate()
            }
        }

    var mRingPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        set(paint) {
            if (paint != field) {
                field = paint
                paint.strokeCap = Paint.Cap.ROUND
                invalidate()
            }
        }

    var mInnerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        set(paint) {
            if (paint != field) {
                field = paint
                invalidate()
            }
        }

    var mProgress: Long = 0
        set(value) {
            if (value != field) {
                field = value
                mListener.onProgressChanged(this)
            }
        }

    var mColor = ContextCompat.getColor(context, android.R.color.white)
        set(color) {
            field = color
            mRingPaint.color = mColor
            invalidate()
        }
    var mDimAlpha = 255

    private var _binding: CircularSeekBarBinding? = null
    val binding get() = _binding!!

    init {
        setWillNotDraw(false)

        background.setTintMode(PorterDuff.Mode.MULTIPLY)

        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircularTimer, 0, 0)
        mColor = a.getColor(R.styleable.CircularTimer_ringColor, mColor)
        mDimAlpha = a.getInt(R.styleable.CircularTimer_ringAlpha, mDimAlpha)
        a.recycle()

        mRingPaint.style = Paint.Style.FILL
        mInnerCirclePaint.style = Paint.Style.FILL
    }

    private fun getDiameter(): Float {
        return width.coerceAtMost(height).toFloat()
    }

    private fun getOuterCircleRadius(): Float {
        return getDiameter() / 2f
    }

    private fun getCenter(): PointF {
        return PointF(width / 2f, height / 2f)
    }

    var mMinValue: Long = 0
        set(value) {
            if (value != field) {
                field = value
                mDefaultValue = value
                if (value > mProgress)
                    mProgress = value
            }
        }

    var mMaxValue: Long = 180L * minuteToMillis   // max = 180 minutes
        set(value) {
            if (value != field) {
                field = value
                if (value < mProgress)
                    mProgress = value
            }
        }

    var mDefaultValue = mMinValue

    private fun updateProgress(angle: Float) {
        // calculate the new value depending on angle
        val newVal = roundProgress(mStart + (angle * mTurnValue / 360f).toLong())
        mProgress = newVal.coerceIn(mMinValue, mMaxValue)
    }

    fun roundProgress(value: Long): Long {
        return ((value + mIncrement / 2) / mIncrement) * mIncrement
    }

    fun progressToAngle(progress: Long): Float {
        return (progress * 360 / mTurnValue) % 360f
    }

    private fun distanceToCenter(x: Float, y: Float): Float {
        val c = getCenter()
        return sqrt((x - c.x).pow(2) + (y - c.y).pow(2))
    }

    fun startRing(event: MotionEvent): Long{
        mAngleTracker.clear()
        mAngleTracker.startMovement(event)
        mStart = roundProgress((mAngleTracker.position * mTurnValue / 360f).toLong())
        updateRing(event)
        return mStart
    }
    fun updateRing(event: MotionEvent){
        mAngleTracker.addMovement(event)
        mTouchAngle = mAngleTracker.position
        invalidate()
    }
    fun updateRing(progress: Long){
        mTouchAngle = progressToAngle(progress)
        invalidate()
    }



    var mTurnValue = 60 * minuteToMillis

    fun setFillColor() {
        if (!TimeService.isRunning) {
            mRingPaint.color = mColor
            mRingPaint.alpha = mDimAlpha
            return
        }
        val aa = progressToAngle(TimeService.remainingTime * 60) / 180f * Math.PI.toFloat()
        if (aa < drawAngle) {
            mRingPaint.color = mColor
            mRingPaint.alpha = mDimAlpha / 3
        } else {
            mRingPaint.color = mColor
            mRingPaint.alpha = mDimAlpha
        }
    }

    fun setOutlineColor() {
        if (!TimeService.isRunning) {
            mRingPaint.color = Color.BLACK
            mRingPaint.alpha = mDimAlpha
            return
        }
        val aa = progressToAngle(TimeService.remainingTime * 60) / 180f * Math.PI.toFloat()
        if (aa < drawAngle) {
            mRingPaint.color = Color.BLACK
            mRingPaint.alpha = mDimAlpha / 3
        } else {
            mRingPaint.color = Color.BLACK
            mRingPaint.alpha = mDimAlpha
        }
    }

    //region Touches
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mEnabled) {
            // if the detector recognized a gesture, consume it
            if (mGestureDetector.onTouchEvent(event)) {
                return true
            }

            // touch gestures only work when touches started exactly on the bar/arc
            if (mStarted) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> trackTouchStart(event)
                    MotionEvent.ACTION_MOVE -> trackTouchMove(event)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> trackTouchStop()
                }
            }
            invalidate()
            return true
        } else {
            return super.onTouchEvent(event)
        }
    }

    private fun trackTouchStart(event: MotionEvent) {
        Settings.cancelEndTime()
        mStart = startRing(event)
        updateProgress(mAngleTracker.angle)
        mListener.onStartTrackingTouch(this)
    }

    private fun trackTouchMove(event: MotionEvent) {
        updateRing(event)
        updateProgress(mAngleTracker.angle)
        mListener.onProgressChanged(this)
    }

    private fun trackTouchStop() {
        mStarted = false
        mListener.onStopTrackingTouch(this)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        // touch gestures only work when touches are made exactly on the bar/arc
        override fun onDown(event: MotionEvent): Boolean {
            val distance = distanceToCenter(event.x, event.y)
            mStarted = distance <= getOuterCircleRadius()
            return false
        }
    }
    //endregion

    //region Lifecycle
    override fun onSizeChanged(xNew: Int, yNew: Int, xOld: Int, yOld: Int) {
        super.onSizeChanged(xNew, yNew, xOld, yOld)
        mAngleTracker = AngleTracker(getCenter().x, getCenter().y)
    }

    var drawAngle = 0f
    var sinAngle = 0f
    var cosAngle = 0f
    var ccx = 0f
    var ccy = 0f
    var size = 0f
    val defSize = screenSize(context).x / 60f
    override fun onDraw(c: Canvas) {
        super.onDraw(c)

        ccx = width / 2f
        ccy = height / 2f
        //mRingPaint.strokeWidth = 10f
        mRingPaint.strokeCap = Paint.Cap.ROUND
        var r: Float
        for (i in 0 until 360 step 6) {
            drawAngle = Math.toRadians(i.toDouble()).toFloat()
            mTouchAngle = when {
                mStarted -> min(
                    min(abs(mAngleTracker.position - i - 360) % 360, defSize),
                    min(abs(mAngleTracker.position - i + 360) % 360, defSize)
                )
                TimeService.isRunning -> min(
                    min(abs(progressToAngle(TimeService.remainingTime) - i - 360) % 360, defSize),
                    min(abs(progressToAngle(TimeService.remainingTime) - i + 360) % 360, defSize)
                )
                else -> 0f
            }

            sinAngle = sin(drawAngle)
            cosAngle = cos(drawAngle)
            if (i % 30 != 0) {
                r = width / 2f
                size = if (mStarted || TimeService.isRunning)
                    defSize / 2f + defSize * 2f * sin(.5f-mTouchAngle/defSize/2f)
                else
                    defSize / 2f
                setOutlineColor()
                c.drawCircle(
                    ccx + (r - size) * sinAngle,
                    ccy - (r - size) * cosAngle,
                    size + 4,
                    mRingPaint
                )
                setFillColor()
                c.drawCircle(
                    ccx + (r - size) * sinAngle,
                    ccy - (r - size) * cosAngle,
                    size,
                    mRingPaint
                )
            } else {
                size = if (mStarted || TimeService.isRunning)
                    .8f + mTouchAngle / defSize / 10f
                else
                    .9f
                r = width / 2f - mRingPaint.strokeWidth / 2f
                setOutlineColor()
                mRingPaint.strokeWidth = 1 / size.pow(3f) * defSize + 4f
                c.drawLine(
                    ccx + r * sinAngle, ccy - r * cosAngle,
                    ccx + r * sinAngle * size, ccy - r * cosAngle * size,
                    mRingPaint
                )
                setFillColor()
                mRingPaint.strokeWidth = 1 / size.pow(3f) * defSize
                c.drawLine(
                    ccx + r * sinAngle, ccy - r * cosAngle,
                    ccx + r * sinAngle * size, ccy - r * cosAngle * size,
                    mRingPaint
                )
            }
        }
    }
    //endregion
}