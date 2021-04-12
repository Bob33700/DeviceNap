@file:Suppress("unused")

package com.bluegecko.sieste

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.media.AudioManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import java.util.concurrent.TimeUnit


// region View
//-------------
// Dialog & Activity windows widh
// retrieve screen dimensions and adjust view width ro x percent of total screen width
fun View.percentWidth(percent: Float) {
    minimumWidth = (screenSize(context).x * percent).toInt()
}

// retrieve screen dimensions and adjust view width ro x percent of total screen height
fun View.percentHeight(percent: Float) {
    minimumHeight = (screenSize(context).y * percent).toInt()
}

// endregion


// region Activity
//-------------
fun Activity.showToast(text: String) {
    runOnUiThread { Toast.makeText(this, text, Toast.LENGTH_SHORT).show() }
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

// endregion


// region Screen
//-------------
@Suppress("DEPRECATION")
fun screenSize(context: Context): Point {
    val size = Point()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val wm = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        with(wm.currentWindowMetrics.bounds) {
            size.set(width(), height())
        }
    } else {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            .getSize(size)
    }
    return size
}

fun convertSpToPixels(context: Context, sp: Number): Int {
    return TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_SP,
		sp.toFloat(),
		context.resources.displayMetrics
	).toInt()
}

fun convertDpToPixels(context: Context, dp: Number): Int {
    return TypedValue.applyDimension(
		TypedValue.COMPLEX_UNIT_DIP,
		dp.toFloat(),
		context.resources.displayMetrics
	)
        .toInt()
}

fun convertPixelsToDp(context: Context, px: Number): Float {
    return px.toFloat() / (context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)
}

fun convertPixelsToSp(context: Context, px: Float): Float {
    return px / (context.resources.displayMetrics.scaledDensity)
}

fun setFullScreen(activity: Activity) {
    if (Build.VERSION.SDK_INT < 30) {
        @Suppress("DEPRECATION")
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.setDecorFitsSystemWindows(true)
            activity.window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars())
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//					or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//					or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//					or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//					or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    )
        }

    }
}
// endregion

// region time formatting
//-------------
val hoursToMinutes = TimeUnit.HOURS.toMinutes(1L)
val minuteToMillis = TimeUnit.MINUTES.toMillis(1L)
fun Long.millisToHHMM(): String {
    var v = this / minuteToMillis
    val hours = v / hoursToMinutes; v -= hours * hoursToMinutes
    val minutes = v
    return String.format("%02d:%02d", hours, minutes)
}

val hourToSeconds = TimeUnit.HOURS.toSeconds(1L)
val minuteToSeconds = TimeUnit.MINUTES.toSeconds(1L)
val secondToMillis = TimeUnit.SECONDS.toMillis(1L)
fun Long.millisToHHMMSS(): String {
    var v = this / secondToMillis
    val hours = v / hourToSeconds; v -= hours * hourToSeconds
    val minutes = v / minuteToSeconds; v -= minutes * minuteToSeconds
    val seconds = v
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
fun Long.millisToHMMSS(): String {
    var v = this / secondToMillis
    val hours = v / hourToSeconds; v -= hours * hourToSeconds
    val minutes = v / minuteToSeconds; v -= minutes * minuteToSeconds
    val seconds = v
    return String.format("%01d:%02d:%02d", hours, minutes, seconds)
}
fun Long.millisToMMSS(): String {
    var v = this / secondToMillis
    val minutes = v / minuteToSeconds; v -= minutes * minuteToSeconds
    val seconds = v
    return String.format("%02d:%02d", minutes, seconds)

}
// endregion


enum class RingMode {SLEEP, AWAKE}
fun setRingMode(mode: RingMode){
    getSystemService(App.instance, AudioManager::class.java)?.ringerMode = when (mode){
        RingMode.SLEEP -> when(Settings.getRingerMode()){
            0 -> AudioManager.RINGER_MODE_VIBRATE
            else -> AudioManager.RINGER_MODE_SILENT
        }
        RingMode.AWAKE -> when(Settings.getRingerMode()){
            1 -> AudioManager.RINGER_MODE_VIBRATE
            else -> AudioManager.RINGER_MODE_NORMAL
        }
    }
}