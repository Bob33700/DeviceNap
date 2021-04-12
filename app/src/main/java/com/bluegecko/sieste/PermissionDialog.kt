package com.bluegecko.sieste

import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import androidx.core.content.ContextCompat.startActivity
import com.bluegecko.sieste.databinding.FragmentPermissionBinding

class PermissionDialog(private val a: Activity) : Dialog(a) {

    private lateinit var binding: FragmentPermissionBinding
    private var okPressed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPermissionBinding.inflate(layoutInflater, null, false)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)

        binding.okButton.setOnClickListener {
            okPressed = true
            requestNotificationPolicyAccess()
            dismiss()
        }
    }

    private fun requestNotificationPolicyAccess() {
            startActivity(context, Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS),null)
    }

    override fun onDetachedFromWindow() {
        if (!isNotificationPolicyAccessGranted() && !okPressed)
            a.finish()
        super.onDetachedFromWindow()
    }

    companion object{
        fun isNotificationPolicyAccessGranted(): Boolean {
            val notificationManager = App.instance.getSystemService(NotificationManager::class.java)
            return notificationManager.isNotificationPolicyAccessGranted
        }
    }
}