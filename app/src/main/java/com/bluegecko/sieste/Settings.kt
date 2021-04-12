package com.bluegecko.sieste

import android.content.Context

class Settings {

    companion object {
        private var prefs = App.instance.getSharedPreferences(App.instance.getString(R.string.settings), Context.MODE_PRIVATE)
        private const val NO_VALUE = -1L
        private const val ALARM_TIME = "ALARM_TIME"
        private const val RINGER_MODE = "RINGER_MODE"

        fun getEndTime(): Long {
            return prefs.getLong(ALARM_TIME, NO_VALUE)
        }

        fun setEndTime(value: Long) {
            prefs.edit().putLong(ALARM_TIME, value).apply()
        }

        fun cancelEndTime(){
            setEndTime(NO_VALUE)
        }

        fun getRingerMode(): Int {
            return prefs.getInt(RINGER_MODE, 0)
        }

        fun setRingerMode(value: Int) {
            prefs.edit().putInt(RINGER_MODE, value).apply()
        }
    }
}