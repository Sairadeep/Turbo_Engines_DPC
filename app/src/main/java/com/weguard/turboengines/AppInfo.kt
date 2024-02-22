package com.weguard.turboengines

import android.graphics.Bitmap

data class AppInfo(
    val appPackage: String,
    val appName: String,
    val appIcon: Bitmap
) {
}