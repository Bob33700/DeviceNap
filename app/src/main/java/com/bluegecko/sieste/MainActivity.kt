package com.bluegecko.sieste

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bluegecko.sieste.PermissionDialog.Companion.isNotificationPolicyAccessGranted
import com.bluegecko.sieste.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity(), CircularTimer.CircularTimerListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !isNotificationPolicyAccessGranted())
            PermissionDialog(this).show()

        NotificationUtils.createNotificationChannel(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFullScreen(this)

        binding.durationPicker.setCircularTimerListener(this)

        if (TimeService.isRunning) {
            if (TimeService.instance == null)
                startService(Intent(this, TimeService::class.java))
            binding.durationPicker.mEnabled = false
            binding.sleepMode.visibility = INVISIBLE
            binding.durationPicker.mShowResetButton = true
        } else {
            binding.sleepMode.visibility = VISIBLE
            binding.durationPicker.mShowResetButton = false
        }
        TimeService.mListener = binding.durationPicker

        binding.sleepMode.setOnClickListener {
            Settings.setRingerMode((Settings.getRingerMode()+1)%3)
            setRingModeButton()
        }

        binding.info.setOnClickListener {
            startActivity(Intent(this, InfoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        setRingModeButton()
    }

    // apparence du bouton de choix du mode
    private fun setRingModeButton(){
        binding.sleepMode.background = ContextCompat.getDrawable(
            this,
            when(Settings.getRingerMode()){
                1 -> R.drawable.mode1
                2 -> R.drawable.mode2
                else -> R.drawable.mode0
            })
    }

    // Actions lorsqu'on crée un timer
    override fun onTimeSet(seekBar: CircularTimer) {
        // indiquer l'heure de la fin du décompte)
        Settings.setEndTime(System.currentTimeMillis() + seekBar.getMillis())
        // mettre en 'silencieux'
        setRingMode(RingMode.SLEEP)
        // masquer le bouton 'choix du mode silencieux'
        binding.sleepMode.visibility = INVISIBLE
        // créer le scheduler (animation du cadran + notification périodique)
        TimeService.startTimeService(this, binding.durationPicker)
    }

    // actions lorsqu'un timer se termine (ou qu'on l'annule)
    override fun onCancel(seekBar: CircularTimer) {
        runOnUiThread {
            binding.sleepMode.visibility = VISIBLE
        }
        // restaurer la sonnerie
        setRingMode(RingMode.AWAKE)
        // effacer la notification courante
        NotificationUtils.cancelNotification()
    }
}