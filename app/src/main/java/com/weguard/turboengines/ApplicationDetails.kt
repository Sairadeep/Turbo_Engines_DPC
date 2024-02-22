package com.weguard.turboengines

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun applicationsDetails(): List<AppInfo> {
    val myContext = LocalContext.current
    val packM = myContext.packageManager
    val intent = Intent(Intent.ACTION_MAIN)
    intent.addCategory(Intent.CATEGORY_LAUNCHER)
    val appsList = packM.queryIntentActivities(intent, PackageManager.GET_META_DATA)
    val apps = appsList.map {
        val packageName = it.activityInfo.packageName
        val appName = it.loadLabel(packM).toString()
        val appIcon = it.loadIcon(packM).toBitmap()
        AppInfo(packageName, appName, appIcon)
    }
    val appsInOrder = apps.sortedBy {
        it.appName.lowercase()
    }
    return appsInOrder
}