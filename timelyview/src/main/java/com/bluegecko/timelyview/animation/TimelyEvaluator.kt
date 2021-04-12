package com.bluegecko.timelyview.animation

import com.nineoldandroids.animation.TypeEvaluator

class TimelyEvaluator : TypeEvaluator<Array<FloatArray>> {
    private var _cachedPoints: Array<FloatArray> = Array(0) { FloatArray(2) }
    override fun evaluate(
        fraction: Float,
        startValue: Array<FloatArray>,
        endValue: Array<FloatArray>
    ): Array<FloatArray> {
        val pointsCount = startValue.size
        initCache(pointsCount)
        for (i in 0 until pointsCount) {
            _cachedPoints[i][0] =
                startValue[i][0] + fraction * (endValue[i][0] - startValue[i][0])
            _cachedPoints[i][1] =
                startValue[i][1] + fraction * (endValue[i][1] - startValue[i][1])
        }
        return _cachedPoints
    }

    private fun initCache(pointsCount: Int) {
        if (_cachedPoints.size != pointsCount) {
            _cachedPoints = Array(pointsCount) { FloatArray(2) }
        }
    }
}