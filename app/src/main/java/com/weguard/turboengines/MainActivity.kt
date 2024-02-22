package com.weguard.turboengines

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.weguard.turboengines.ui.theme.TurboEnginesTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TurboEnginesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TEDPC()
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Preview(showBackground = true)
@Composable
fun TEDPC() {
    // DO command -> adb shell dpm set-device-owner <packageName>/.<adminReceiver>
    // adb shell dpm set-device-owner com.weguard.turboengines/.TEAdminReceiver

    val context = LocalContext.current
    val buttonState = remember { mutableStateOf(true) }
    val bottomSheetStatus = remember {
        mutableStateOf(false)
    }
    val bottomBarState = remember { mutableStateOf(true) }
    val appDrawerButtonState = remember { mutableStateOf(true) }
    val homeLauncherButtonState = remember { mutableStateOf(true) }
    val topAppBarState = remember { mutableStateOf(true) }
    val app = applicationsDetails()
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(context, TEAdminReceiver::class.java)
//    val custom = ComponentName(context, MainActivity::class.java)
    val filter = IntentFilter()
    filter.addAction(Intent.ACTION_MAIN)
    filter.addAction(Intent.CATEGORY_HOME)
    filter.addAction(Intent.ACTION_DEFAULT)
    Scaffold(
        topBar = {
            if (topAppBarState.value) {
                TopAppBar(
                    title = { Text(text = "Turbo Engines", fontSize = 24.sp) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(id = R.color.purple_200),
                        titleContentColor = Color.Black
                    ),
                    actions = {
                        Button(
                            enabled = buttonState.value,
                            onClick = {
                                val customLauncher =
                                    ComponentName(context, MainActivity::class.java)

                                // enable custom launcher (it's disabled by default in manifest)
                                context.packageManager.setComponentEnabledSetting(
                                    customLauncher,
                                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP
                                )

                                // set custom launcher as default home activity
//                                devicePolicyManager.addPersistentPreferredActivity(
//                                    componentName, filter, customLauncher
//                                )
//                                devicePolicyManager.clearPackagePersistentPreferredActivities(
//                                    componentName,
//                                    "com.weguard.turboengines"
//                                )
//                                devicePolicyManager.addPersistentPreferredActivity(
//                                    componentName, filter, custom
//                                )
//                                if (devicePolicyManager.isAdminActive(componentName)) {
//                                    Toast.makeText(context, "Hi", Toast.LENGTH_SHORT).show()
//                                }
                            },
                            modifier = Modifier.size(150.dp, 35.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Blue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Home Launcher",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick =
                            {
                                // After reboot same app needs to be launched.
                                devicePolicyManager.reboot(componentName)
                            }
                        ) {
                            Icon(
                                Icons.TwoTone.Refresh,
                                contentDescription = "Reboot",
                                tint = Color.Black
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (bottomBarState.value) {
                BottomAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(15.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = {
                            bottomSheetStatus.value = true
                            appDrawerButtonState.value = false
                            homeLauncherButtonState.value = false
                            topAppBarState.value = false
                            bottomBarState.value = false
                        }) {
                            if (appDrawerButtonState.value) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = "App Drawer",
                                    tint = Color.Black,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.tree),
                contentDescription = "",
                Modifier.fillMaxSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                // To Do
            }
        }
        AppDrawer(
            bottomSheetStatus,
            appDrawerButtonState,
            homeLauncherButtonState,
            topAppBarState,
            bottomBarState,
            context,
            app
        )
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
private fun AppDrawer(
    bottomSheetStatus: MutableState<Boolean>,
    appDrawerButtonState: MutableState<Boolean>,
    homeLauncherButtonState: MutableState<Boolean>,
    topAppBarState: MutableState<Boolean>,
    bottomBarState: MutableState<Boolean>,
    context: Context,
    app: List<AppInfo>
) {
    if (bottomSheetStatus.value) {
        ModalBottomSheet(
            onDismissRequest = {
                bottomSheetStatus.value = false
                appDrawerButtonState.value = true
                homeLauncherButtonState.value = true
                topAppBarState.value = true
                bottomBarState.value = true
            },
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(30.dp),
            scrimColor = Color.Transparent,
            sheetState = SheetState(
                skipPartiallyExpanded = true,
                skipHiddenState = false
            ),
            contentColor = Color.White,
            containerColor = Color.Transparent
        ) {
            context.display?.mode?.toString()?.let { it1 -> Log.d("Current Mode", it1) }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(205.dp),
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center
            ) {
                items(
                    count = app.size,
                    itemContent = { index ->
                        Column(
                            modifier = Modifier
                                .height(200.dp)
                                .padding(10.dp)
                                .combinedClickable(
                                    onClick = {
                                        val intentToLaunch =
                                            context.packageManager.getLaunchIntentForPackage(
                                                app[index].appPackage
                                            )
                                        if (intentToLaunch != null) context.startActivity(
                                            intentToLaunch
                                        ) else Toast
                                            .makeText(
                                                context,
                                                "Unable to open app",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                        Log.d(
                                            "ClickHappenedOn", app[index].appName
                                        )
                                    },
                                    enabled = true
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                bitmap = (app[index].appIcon).asImageBitmap(),
                                contentDescription = "",
                                modifier = Modifier.size(100.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = app[index].appName,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    softWrap = true,
                                    modifier = Modifier.fillMaxSize(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}



