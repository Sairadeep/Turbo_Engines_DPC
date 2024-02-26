package com.weguard.turboengines

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val bootStatus = intent.getBooleanExtra("state", false)
        if (bootStatus) {
            Log.d("BootStatus", "Boot Completed")
        } else {
            Log.d("BootStatus", "Boot Not Completed")
        }
    }
}