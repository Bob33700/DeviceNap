package com.bluegecko.sieste

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bluegecko.sieste.databinding.CircularSeekBarBinding
import com.bluegecko.timelyview.TimelyHHMM2View

@Suppress("PrivateResource", "unused", "MemberVisibilityCanBePrivate")
class CircularTimer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    ConstraintLayout(context, attrs, defStyleAttr),
    CircularTimerRing.OnRingChangeListener,
    TimeService.TimeServiceListener {

    /**
     * Listen for touch-events on the ring area
     */
    interface CircularTimerListener {
        fun onTimeSet(seekBar: CircularTimer)
        fun onCancel(seekBar: CircularTimer)
    }

    val progressLabel: TimelyHHMM2View
    val ring: CircularTimerRing

    // private
    private var mStart: Long

    // settable by the client programmatically
    private var mCircularTimerListener: CircularTimerListener? = null

    // settable by the client through attributes and programmatically
    var mEnabled = true
        set(enabled) {
            if (enabled != field) {
                field = enabled
                alpha = if (enabled) 1f else .7f
                ring.mEnabled = enabled
                invalidate()
            }
        }

    var mShowIndicator = true
        set(enable) {
            if (enable != field) {
                field = enable
                invalidate()
            }
        }

    var mProgressTextColor = Color.BLACK
        set(color) {
            if (color != field) {
                field = color
                progressLabel.textColor = color
                invalidate()
            }
        }

    var mShowText = true
        set(enabled) {
            if (enabled != field) {
                field = enabled
                progressLabel.visibility = if (enabled) VISIBLE else GONE
                invalidate()
            }
        }

    var mShowResetButton = true
        set(enabled) {
            if (enabled != field) {
                field = enabled
                binding.resetButton.visibility = if (enabled) VISIBLE else INVISIBLE
                invalidate()
            }
        }

    // time in millis
    var mProgress: Long = 0
        set(value) {
            if (value != field) {
                field = value
                setProgressLabelValue()
            }
        }

    private var _binding: CircularSeekBarBinding? = null
    val binding get() = _binding!!

    init {
        setWillNotDraw(false)

        _binding = CircularSeekBarBinding.inflate(LayoutInflater.from(context), this, true)

        ring = binding.ring
        progressLabel = binding.csbProgressTextView


        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CircularTimer, 0, 0)

        mEnabled = a.getBoolean(R.styleable.CircularTimer_enabled, mEnabled)
        mShowIndicator = a.getBoolean(R.styleable.CircularTimer_showIndicator, mShowIndicator)
        mProgressTextColor =
            a.getColor(R.styleable.CircularTimer_progressTextColor, mProgressTextColor)
        mShowText = a.getBoolean(R.styleable.CircularTimer_showProgressText, mShowText)
        mShowResetButton = a.getBoolean(R.styleable.CircularTimer_showResetButton, mShowResetButton)

        ring.apply{
            circularTimer = this@CircularTimer
            mListener = this@CircularTimer
            mRingPaint.style = Paint.Style.FILL
            mDimAlpha = a.getInt(R.styleable.CircularTimer_ringAlpha, ring.mDimAlpha)
            mTurnValue =
                a.getInt(R.styleable.CircularTimer_turnValue, ring.mTurnValue.toInt()).toLong()
            mMinValue = a.getInt(R.styleable.CircularTimer_min, ring.mMinValue.toInt()).toLong()
            mMaxValue = a.getInt(R.styleable.CircularTimer_max, ring.mMaxValue.toInt()).toLong()
            mDefaultValue =
                a.getInt(R.styleable.CircularTimer_defaultValue, ring.mDefaultValue.toInt()).toLong()
            mIncrement =
                a.getInt(R.styleable.CircularTimer_increment, ring.mIncrement.toInt()).toLong()
            mColor = a.getColor(R.styleable.CircularTimer_ringColor, ring.mColor)
            mProgress =
                a.getInt(R.styleable.CircularTimer_progress, ring.mProgress.toInt()).toLong()
        }

        a.recycle()

        mProgress = 0
        mStart = mProgress
        progressLabel.strokeWidth = 10f

        binding.resetButton.visibility = VISIBLE
        binding.resetButton.setOnClickListener {
            TimeService.instance?.cancel()
        }
    }

    fun setProgressLabelValue() {
        progressLabel.setTime(mProgress)
        progressLabel.invalidate()
    }

    fun reset() {
        mEnabled = true             // cadran activé
        mProgress = 0               // chiffres à zéro
        ring.invalidate()           // redessiner cadran
        mShowResetButton = false    // masquer le bouton 'STOP'
    }


    //region Public listener
    /**
     * Set a listener for touch-events related to the outer ring of the CircularSeekBar
     * @param listener
     */
    fun setCircularTimerListener(listener: CircularTimerListener?) {
        mCircularTimerListener = listener
    }
    //endregion

    //region Public attribute
    fun getMillis(): Long {
        return mProgress
    }

    override fun onProgressChanged(ring: CircularTimerRing) {
        mProgress = ring.mProgress      // actualisation des chiffres
    }

    override fun onStartTrackingTouch(ring: CircularTimerRing) {
        binding.root.findFocus()?.clearFocus()
        mStart = ring.mStart            // initialisation du temps initial
        mProgress = ring.mProgress      // initialisation des chiffres
    }

    override fun onStopTrackingTouch(ring: CircularTimerRing) {
        // si le temps est non nul
        if (mProgress>0){
            mEnabled = false            // cadran inactivé
            mShowResetButton = true     // afficher le bouton 'STOP'
            // signal à 'MainActivity'
            mCircularTimerListener?.onTimeSet(this)
        }
     }

    override fun onTimeServiceChanged(time: Long) {
        // couleurs sombres
        binding.root.background = ContextCompat.getDrawable(context, R.color.background2)
        // mise à jour de l'affichage
        mProgress = time        // chiffres
        ring.updateRing(time)   // cadran
        invalidate()
    }

    override fun onTimeServiceCancel() {
        // signal à 'MainActivity'
        mCircularTimerListener?.onCancel(this)
        // couleurs claires
        binding.root.background = ContextCompat.getDrawable(context, R.color.background)
        // mise à jour de l'affichage
        reset()
    }
    //endregion
}