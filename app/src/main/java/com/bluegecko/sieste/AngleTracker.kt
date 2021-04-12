package com.bluegecko.sieste

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

class AngleTracker internal constructor(private val mCentreX: Float, private val mCentreY: Float) {

	private var mInitialX: Float = 0f
	private var mInitialY: Float = 0f
	private var mFinalX: Float = 0f
	private var mFinalY: Float = 0f
	private var mCount = 0f

	val position = calcAngle(mFinalX, mFinalY)

	val angle: Float
		get() {
			var retVal = 0f
			val initialAngle = calcAngle(mInitialX, mInitialY)
			val finalAngle = position
			if (abs(finalAngle - initialAngle) < 60) {
				retVal = (finalAngle - initialAngle)
			}
			mCount += retVal
			return mCount
		}

	internal fun startMovement(event: MotionEvent){
		mInitialX = event.x
		mInitialY = event.y
		mFinalX = event.x
		mFinalY = event.y
		mCount = 0f
	}

	internal fun addMovement(event: MotionEvent) {
		mInitialX = mFinalX
		mInitialY = mFinalY
		mFinalX = event.x
		mFinalY = event.y
	}

	internal fun clear() {
		mInitialX = 0f
		mInitialY = 0f
		mFinalX = 0f
		mFinalY = 0f
		mCount = 0f
	}

	private fun calcAngle(x: Float, y: Float): Float {
		return (-Math.toDegrees(atan2((mCentreX - x).toDouble(), (mCentreY - y).toDouble())).toFloat() + 360) %360
	}
}
