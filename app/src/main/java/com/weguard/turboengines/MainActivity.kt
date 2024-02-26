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
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
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

//    var filter = IntentFilter()
//    val receiver = MyBootReceiver()

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

//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onStart() {
//        filter.addAction(Intent.ACTION_REBOOT)
//        this.registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
//        super.onStart()
//    }
//
//    override fun onStop() {
//        unregisterReceiver(receiver)
//        super.onStop()
//    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @Preview(showBackground = true)
    @Composable
    fun TEDPC() {
//         DO command -> adb shell dpm set-device-owner <packageName>/.<adminReceiver>
//         adb shell dpm set-device-owner com.weguard.turboengines/.TEAdminReceiver

        val context = LocalContext.current
//      val buttonState = remember { mutableStateOf(true) }
        val bottomSheetStatus = remember {
            mutableStateOf(false)
        }
        val lockTaskButtonText = remember { mutableStateOf("Enable Lock Task Mode") }
        val lockTaskStatus = remember { mutableStateOf(false) }
        val contextAsComponentActivity =
            (context as ComponentActivity)
        val bottomBarState = remember { mutableStateOf(true) }
        val iconSize = remember {
            mutableIntStateOf(0)
        }
        val appDrawerButtonState = remember { mutableStateOf(true) }
        val homeLauncherButtonState = remember { mutableStateOf(true) }
        val topAppBarState = remember { mutableStateOf(true) }
        val app = applicationsDetails()
        val appsCount = app.size
        val packageManager = context.packageManager
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, TEAdminReceiver::class.java)
        val appPackages = arrayListOf<String>()
//        val custom = ComponentName(context, MainActivity::class.java)
//        val filter = IntentFilter()

        CheckLockTaskAndAllowlistOfApps(
            appsCount,
            appPackages,
            app,
            devicePolicyManager,
            componentName
        )

        Scaffold(
            topBar = {
                if (topAppBarState.value) {
                    TopAppBar(
                        devicePolicyManager,
                        context,
                        lockTaskStatus,
                        contextAsComponentActivity,
                        lockTaskButtonText,
                        componentName
                    )
                }
            },
            bottomBar = {
                if (bottomBarState.value) {
                    BottomAppBar(
                        bottomSheetStatus,
                        appDrawerButtonState,
                        homeLauncherButtonState,
                        topAppBarState,
                        bottomBarState
                    )
                }
            }
        )
        {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HomeBackgroundImage()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    // To Do
                }
            }
            if (bottomSheetStatus.value) {
                AppsListModalButtonSheet(
                    bottomSheetStatus,
                    appDrawerButtonState,
                    homeLauncherButtonState,
                    topAppBarState,
                    bottomBarState,
                    context,
                    packageManager,
                    iconSize,
                    app
                )
            }
        }
    }

    @Composable
    private fun CheckLockTaskAndAllowlistOfApps(
        appsCount: Int,
        appPackages: ArrayList<String>,
        app: List<AppInfo>,
        devicePolicyManager: DevicePolicyManager,
        componentName: ComponentName
    ) {
        // Check whether lock task is permitted or not.
        for (x in 0..<appsCount) {
            appPackages.add(app[x].appPackage)
        }
        Log.d("App PAcKAHGe", appPackages.toString())
        devicePolicyManager.setLockTaskPackages(
            componentName,
            appPackages.toTypedArray()
        )
    }

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun TopAppBar(
        devicePolicyManager: DevicePolicyManager,
        context: ComponentActivity,
        lockTaskStatus: MutableState<Boolean>,
        contextAsComponentActivity: ComponentActivity,
        lockTaskButtonText: MutableState<String>,
        componentName: ComponentName
    ) {
        TopAppBar(
            title = { Text(text = "Turbo Engines", fontSize = 24.sp) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(id = R.color.purple_200),
                titleContentColor = Color.Black
            ),
            actions = {

                // Lock Task Mode
                LockTaskMode(
                    devicePolicyManager,
                    context,
                    lockTaskStatus,
                    contextAsComponentActivity,
                    lockTaskButtonText
                )

//                HomeLauncher(
//                    filter,
//                    buttonState,
//                    context,
//                    devicePolicyManager,
//                    componentName,
//                    custom
//                )

                Reboot(devicePolicyManager, componentName)
            }
        )
    }

    @Composable
    private fun LockTaskMode(
        devicePolicyManager: DevicePolicyManager,
        context: ComponentActivity,
        lockTaskStatus: MutableState<Boolean>,
        contextAsComponentActivity: ComponentActivity,
        lockTaskButtonText: MutableState<String>
    ) {
        Button(
            onClick = {
                toggleLockTaskMode(
                    devicePolicyManager,
                    context,
                    lockTaskStatus,
                    contextAsComponentActivity,
                    lockTaskButtonText
                )
            },
        ) {
            Text(text = lockTaskButtonText.value)
        }
    }

    private fun toggleLockTaskMode(
        devicePolicyManager: DevicePolicyManager,
        context: ComponentActivity,
        lockTaskStatus: MutableState<Boolean>,
        contextAsComponentActivity: ComponentActivity,
        lockTaskButtonText: MutableState<String>
    ) {
        if (devicePolicyManager.isLockTaskPermitted(context.packageName)) {

            lockTaskStatus.value = !(lockTaskStatus.value)

            when (lockTaskStatus.value) {

                true -> {
                    contextAsComponentActivity.startLockTask()
                    lockTaskButtonText.value = "Exit Lock Task Mode"
                }

                false -> {
                    contextAsComponentActivity.stopLockTask()
                    lockTaskButtonText.value = "Enable Lock Task Mode"
                }
            }
        } else {
            Toast.makeText(
                context,
                "Lock Task isn't Allowed",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    private fun AppsListModalButtonSheet(
        bottomSheetStatus: MutableState<Boolean>,
        appDrawerButtonState: MutableState<Boolean>,
        homeLauncherButtonState: MutableState<Boolean>,
        topAppBarState: MutableState<Boolean>,
        bottomBarState: MutableState<Boolean>,
        context: ComponentActivity,
        packageManager: PackageManager,
        iconSize: MutableIntState,
        app: List<AppInfo>
    ) {
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
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)) {
                Toast.makeText(context, "This is TV", Toast.LENGTH_SHORT).show()
                iconSize.intValue = 200
            } else {
                iconSize.intValue = 80
            }
            AppsWithLVG(iconSize, app, context)
        }
    }

    @Composable
    @OptIn(ExperimentalFoundationApi::class)
    private fun AppsWithLVG(
        iconSize: MutableIntState,
        app: List<AppInfo>,
        context: ComponentActivity
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(iconSize.intValue.dp),
            verticalArrangement = Arrangement.Center,
            horizontalArrangement = Arrangement.Center
        ) {
            items(
                count = app.size,
                itemContent = { index ->
                    Column(
                        modifier = Modifier
                            .height(150.dp)
                            .padding(10.dp)
                            .combinedClickable(
                                onClick = {
                                    openAnApp(context, app, index)
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
                            AppName(app, index)
                        }
                    }
                }
            )
        }
    }

    @Composable
    private fun AppName(
        app: List<AppInfo>,
        index: Int
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

    private fun openAnApp(
        context: ComponentActivity,
        app: List<AppInfo>,
        index: Int
    ) {
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
    }

    @Composable
    private fun HomeBackgroundImage() {
        Image(
            painter = painterResource(id = R.drawable.te),
            contentDescription = "",
            Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }

    @Composable
    private fun BottomAppBar(
        bottomSheetStatus: MutableState<Boolean>,
        appDrawerButtonState: MutableState<Boolean>,
        homeLauncherButtonState: MutableState<Boolean>,
        topAppBarState: MutableState<Boolean>,
        bottomBarState: MutableState<Boolean>
    ) {
        BottomAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(75.dp)
                .padding(15.dp)
                .clip(RoundedCornerShape(20.dp)),
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center
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
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun Reboot(
        devicePolicyManager: DevicePolicyManager,
        componentName: ComponentName
    ) {
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

    @Composable
    private fun HomeLauncher(
        filter: IntentFilter,
        buttonState: MutableState<Boolean>,
        context: ComponentActivity,
        devicePolicyManager: DevicePolicyManager,
        componentName: ComponentName,
        custom: ComponentName
    ) {
        filter.addAction(Intent.ACTION_MAIN)
        filter.addAction(Intent.CATEGORY_HOME)
        filter.addAction(Intent.ACTION_DEFAULT)
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
                devicePolicyManager.addPersistentPreferredActivity(
                    componentName, filter, customLauncher
                )
                devicePolicyManager.clearPackagePersistentPreferredActivities(
                    componentName,
                    "com.weguard.turboengines"
                )
                devicePolicyManager.addPersistentPreferredActivity(
                    componentName, filter, custom
                )
                if (devicePolicyManager.isAdminActive(componentName)) {
                    Toast.makeText(context, "Hi", Toast.LENGTH_SHORT).show()
                }
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
    }
}