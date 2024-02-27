package com.weguard.turboengines

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DeviceLockStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val lockState = intent.getBooleanExtra("state", false)
        if (lockState) {
            Toast.makeText(context,"Broadcast Received!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,"Broadcast Not Received!", Toast.LENGTH_SHORT).show()
        }
    }
}