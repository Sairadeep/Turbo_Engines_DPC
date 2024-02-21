package com.weguard.turboengines

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TEAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        Log.d("Enabled","Enabled")
        super.onEnabled(context, intent)
    }
}