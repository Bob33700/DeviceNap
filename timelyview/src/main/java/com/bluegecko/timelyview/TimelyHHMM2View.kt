package com.bluegecko.timelyview

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import java.util.*

/**
 * Created By MBH on 2016-10-05.
 */
class TimelyHHMM2View @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TimelyTimeCommon(context, attrs, defStyleAttr) {
    private var timeFormat = 1 // Default HOUR_MIN;
    private var timeIntArr = intArrayOf(0, 0)

    init {
        inflate(context, R.layout.timely_hhmm2_layout, this)
        hRight = findViewById(R.id.ttv_hours_right)
        minLeft = findViewById(R.id.ttv_minutes_left)
        minRight = findViewById(R.id.ttv_minutes_right)
        arrayTimelyViews = SparseArray()
        arrayTimelyViews.put(1, hRight)
        arrayTimelyViews.put(2, minLeft)
        arrayTimelyViews.put(3, minRight)
        for (i in 0 until arrayTimelyViews.size()) {
            arrayTimelyViews.valueAt(i).setTextColorAndCorner(textColor, isRoundedCorner)
        }
    }

    override val isShortTime: Boolean
        get() = true

    override fun setTimeToTimelyViews(timeArray: IntArray) {
        if (timeArray[0] % 10 != 0) {
            hRight!!.visibility = VISIBLE
            animateCarefully(
                hRight!!,
                if (animationType == ANIMATION_ZOOM) -1 else timeIntArr[0] % 10,
                timeArray[0] % 10
            )
        } else {
            hRight!!.visibility = INVISIBLE
        }
        animateCarefully(
            minLeft!!,
            if (animationType == ANIMATION_ZOOM) -1 else timeIntArr[1] % 100 / 10,
            timeArray[1] % 100 / 10
        )
        animateCarefully(
            minRight!!,
            if (animationType == ANIMATION_ZOOM) -1 else timeIntArr[1] % 10,
            timeArray[1] % 10
        )
        timeIntArr = timeArray
    }

    override fun setTime(date: Date?) {
        TimelyTimeUtils.checkNull(date)
        val cal = Calendar.getInstance()
        cal.time = date!!
        val tempArray = IntArray(2)
        if (timeFormat == FORMAT_HOUR_MIN) {
            tempArray[0] = cal[Calendar.HOUR_OF_DAY]
            tempArray[1] = cal[Calendar.MINUTE]
        } else {
            tempArray[0] = cal[Calendar.MINUTE]
            tempArray[1] = cal[Calendar.SECOND]
        }
        setTimeIfNotEqual(tempArray, timeIntArr)
    }

    override fun setTime(timeIntArray: IntArray?) {
        setTimeChecked(timeIntArray!!, timeIntArr)
    }

    /**
     * Set time formatted from string
     *
     * @param formattedTime: time should be formatted as 00:00
     */
    override fun setTime(formattedTime: String) {
        TimelyTimeUtils.checkNull(formattedTime)
        require(formattedTime.length == 5) { "Time format should be 00:00, not $formattedTime" }
        val splitted = formattedTime.split(":".toRegex()).toTypedArray()
        require(splitted.size == 2) { "Time format should be 00:00, not $formattedTime" }
        val field1 = TimelyTimeUtils.tryParseInt(splitted[0], -1)
        val field2 = TimelyTimeUtils.tryParseInt(splitted[1], -1)
        setTime(intArrayOf(field1, field2))
    }

    override fun setTime(milliseconds: Long) {
        TimelyTimeUtils.checkValidPositiveLong(milliseconds)
        val field1: Int
        val field2: Int
        if (timeFormat == FORMAT_HOUR_MIN) {
            field1 = (milliseconds / (1000 * 60 * 60) % 24).toInt() // Hours
            field2 = (milliseconds / (1000 * 60) % 60).toInt() // minutes
        } else {
            field1 = (milliseconds / (1000 * 60) % 60).toInt() // minutes
            field2 = (milliseconds / 1000).toInt() % 60 // seconds
        }
        TimelyTimeUtils.checkValidPositiveInt(field1)
        TimelyTimeUtils.checkValidPositiveInt(field2)
        setTime(intArrayOf(field1, field2))
    }

    companion object {
        const val FORMAT_HOUR_MIN = 1
    }
}