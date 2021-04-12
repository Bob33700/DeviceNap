package com.bluegecko.timelyview

/**
 * Created By MBH on 2016-10-03.
 */
internal object TimelyTimeUtils {
    fun compareArrays(array1: IntArray?, array2: IntArray?): Boolean {
        if (array1 != null && array2 != null) {
            if (array1.size != array2.size) return false else for (i in array2.indices) {
                if (array2[i] != array1[i]) {
                    return false
                }
            }
        } else {
            return false
        }
        return true
    }

    fun checkNull(`object`: Any?) {
        requireNotNull(`object`) { "Argument cannot be null" }
    }

    fun checkValidPositiveInt(num: Int) {
        require(num >= 0) { "Number cannot be less than zero 0" }
    }

    fun checkValidPositiveLong(num: Long) {
        require(num >= 0) { "Number cannot be less than zero 0" }
    }

    fun checkTimelyTimeArray(timelyArray: IntArray, isShort: Boolean) {
        checkNull(timelyArray)
        val length = if (isShort) 2 else 3
        require(timelyArray.size == length) { "Array should have 3 elements for Hour, Min, Sec" }
        for (timelyInt in timelyArray) {
            require(timelyInt >= 0) { "Time cannot be less than Zero" }
        }
    }

    fun tryParseInt(str: String, def: Int): Int {
        return try {
            str.toInt()
        } catch (e: NumberFormatException) {
            def
        }
    }
}