package com.bluegecko.sieste

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.bluegecko.sieste.NotificationUtils.Companion.notify
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class TimeService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    interface TimeServiceListener {
        fun onTimeServiceChanged(time: Long)
        fun onTimeServiceCancel()
    }

    override fun onCreate() {
        if (instance == null)
            instance = this

        startScheduler()
    }

    private var task: ScheduledFuture<*>? = null
    fun startScheduler() {
        // cancel if already existed
        if (!mScheduler.isTerminated) {
            mScheduler.shutdownNow()
        }
        // create new
        mScheduler = Executors.newScheduledThreadPool(2)
        // schedule task
        task = mScheduler.scheduleAtFixedRate(
            {
                // run on another Thread to avoid crash
                Handler(Looper.getMainLooper()).post {
                    val r = remainingTime
                    if (r>0){                               // s'il reste du temps
                        notify(r)                           //      notification périodique
                        mListener?.onTimeServiceChanged(r)  //      mise à jour de l'affichage
                    } else {                                // lorsque le décompte est terminé
                        cancel()                            //      réinitialisation
                    }
                }
            }, 0, 1, TimeUnit.SECONDS
        )
    }

    fun cancel() {
        // plus de temps restant à décompter
        Settings.cancelEndTime()
        // annuler les notifications périodiques
        mScheduler.shutdownNow()
        // restaurer l'affichage
        mListener?.onTimeServiceCancel()
    }

    companion object {

        var instance: TimeService? = null
        var mListener: TimeServiceListener? = null

        // timer handling
        private var mScheduler = Executors.newScheduledThreadPool(1)

        fun startTimeService(context: Context, listener: TimeServiceListener){
            if (instance==null){
                context.startService(Intent(context, TimeService::class.java))
                mListener = listener
            } else {
                mListener = listener
                instance!!.startScheduler()
            }
        }

        val remainingTime = Settings.getEndTime() - System.currentTimeMillis()

        val isRunning = remainingTime > 0
    }
}