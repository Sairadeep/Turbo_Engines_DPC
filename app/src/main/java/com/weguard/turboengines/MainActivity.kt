package com.weguard.turboengines

import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.weguard.turboengines.ui.theme.TurboEnginesTheme

class MainActivity : ComponentActivity() {

    private val filter = IntentFilter()
    private val deviceLockStateReceiver = DeviceLockStateReceiver()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TurboEnginesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Navigation()
                }
            }
        }
    }

    override fun onStart() {
        filter.addAction(Intent.ACTION_USER_UNLOCKED)
        this.registerReceiver(deviceLockStateReceiver, filter)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(deviceLockStateReceiver)
        super.onStop()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Navigation() {

//    Create a navController
    val navController = rememberNavController()

//    navHost and add composable pages for navigation
    NavHost(navController = navController, startDestination = "HomePage") {

//   Add Navigation Structure using composable functions

        composable(route = "HomePage") {
            TEDPC(navController)
        }

        composable(route = "AdminLockScreen") {
            AdminLock(navController)
        }
    }
}