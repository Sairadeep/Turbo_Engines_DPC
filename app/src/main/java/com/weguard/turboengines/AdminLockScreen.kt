package com.weguard.turboengines

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AdminLock(navController: NavController) {
    val context = LocalContext.current
    val contextAsComponentActivity = (context as ComponentActivity)
    BackHandler (
        enabled = true,
        onBack = {
            Toast.makeText(context,"Back button is disabled", Toast.LENGTH_SHORT).show()
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                Icons.TwoTone.Lock,
                contentDescription = "Admin Lock",
                modifier = Modifier
                    .size(150.dp, 100.dp)
                    .clickable {
                        contextAsComponentActivity.stopLockTask()
                        navController.popBackStack("HomePage", false)
                        Toast
                            .makeText(context, "Admin unlocked", Toast.LENGTH_SHORT)
                            .show()
                    },
                contentScale = ContentScale.FillBounds,
                alignment = Alignment.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Device is admin locked.",color = Color.Black, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        }
    }
}