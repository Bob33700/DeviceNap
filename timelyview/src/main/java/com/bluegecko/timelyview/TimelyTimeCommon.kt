package com.bluegecko.timelyview

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.LinearLayout
import android.widget.TextView
import com.bluegecko.timelyview.model.number.Null
import com.nineoldandroids.animation.ObjectAnimator
import java.util.*

/**
 * Created By MBH on 2016-10-05.
 */
@Suppress("unused")
abstract class TimelyTimeCommon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    protected var arrayTimelyViews: SparseArray<TimelyView> = SparseArray()
    private var arraySeperators: SparseArray<TextView> = SparseArray()
    protected var hLeft: TimelyView? = null
    protected var hRight: TimelyView? = null
    protected var minLeft: TimelyView? = null
    protected var minRight: TimelyView? = null
    protected var seperator1: TextView? = null

    var textColor = Color.WHITE
        set(value) {
            if (value != field) {
                field = value
                setTextColor(arrayTimelyViews, arraySeperators, textColor)
            }
        }
    var isRoundedCorner = true
        set(value) {
            if (value != field) {
                field = value
                setRoundedCorner(arrayTimelyViews, isRoundedCorner)
            }
        }

    private var seperatorsTextSize = 16
        set(value) {
            if (value != field) {
                field = value
                setSeperatorsTextSize(arraySeperators, seperatorsTextSize)
            }
        }


    var strokeWidth = 20.0f
        set(value) {
            require(strokeWidth > 0f) { "Stroke width must be more than 0" }
            if (value != field) {
                field = value
                if (arrayTimelyViews != Null.controlPoints)
                    for (i in 0 until arrayTimelyViews.size()) {
                        arrayTimelyViews.valueAt(i).setStrokeWidth(strokeWidth)
                    }
            }
        }
    protected var animationType = ANIMATION_MATERIAL

    protected fun animateCarefully(timelyView: TimelyView, start: Int, end: Int) {
        val oa: ObjectAnimator? = if (start == -1) {
            timelyView.animateCarefully(end)
        } else {
            timelyView.animateCarefully(start, end)
        }
        oa?.start()
    }

    abstract val isShortTime: Boolean
    abstract fun setTimeToTimelyViews(timeArray: IntArray)
    abstract fun setTime(date: Date?)
    abstract fun setTime(timeIntArray: IntArray?)
    abstract fun setTime(milliseconds: Long)
    abstract fun setTime(formattedTime: String)

    /**
     * Set text color to all timelyviews and textviews(seperators)
     *
     * @param _timelyViews: Array of timely views
     * @param _seperators:  array of seperators
     * @param textColor:    new text color to be set
     */
    private fun setTextColor(
        _timelyViews: SparseArray<TimelyView>?,
        _seperators: SparseArray<TextView>?,
        textColor: Int
    ) {
        for (i in 0 until _timelyViews!!.size()) {
            _timelyViews.valueAt(i).setTextColor(textColor)
        }
        for (i in 0 until _seperators!!.size()) {
            _seperators.valueAt(i).setTextColor(textColor)
        }
    }

    /**
     * This will check the arrays before applying it to the TimelyViews, cuz the array will be provided by user
     *
     * @param newArray:   New array provided by user
     * @param olderArray: Array that present the previous time
     */
    protected fun setTimeChecked(newArray: IntArray, olderArray: IntArray?) {
        TimelyTimeUtils.checkTimelyTimeArray(newArray, isShortTime)
        setTimeIfNotEqual(newArray, olderArray)
    }

    /**
     * Start animation if new time different from the older one, otherwise do nothing
     *
     * @param arrayToShow: Array needs to be shown
     * @param prevArray:   Array that present the previous time
     */
    protected fun setTimeIfNotEqual(arrayToShow: IntArray, prevArray: IntArray?) {
        if (TimelyTimeUtils.compareArrays(arrayToShow, prevArray)) {
            return
        }
        setTimeToTimelyViews(arrayToShow)
        invalidate()
    }

    /**
     * Will change the (":") seperators text size which is not included in the timely views
     *
     * @param _seperators:        seperators textviews
     * @param seperatorsTextSize: text size to be set
     */
    private fun setSeperatorsTextSize(
        _seperators: SparseArray<TextView>?,
        seperatorsTextSize: Int
    ) {
        for (i in 0 until _seperators!!.size()) {
            _seperators.valueAt(i).textSize = seperatorsTextSize.toFloat()
        }
    }

    private fun setRoundedCorner(_timelyViews: SparseArray<TimelyView>?, isRoundedCorner: Boolean) {
        for (i in 0 until _timelyViews!!.size()) {
            _timelyViews.valueAt(i).setRoundedCorner(isRoundedCorner)
        }
    }


    companion object {
        const val ANIMATION_ZOOM = 1
        const val ANIMATION_MATERIAL = 2
    }
}