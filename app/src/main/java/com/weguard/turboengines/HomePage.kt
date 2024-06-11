package com.weguard.turboengines

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.Refresh
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

@OptIn(ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun TEDPC(navController: NavController) {

//    DO command -> adb shell dpm set-device-owner <packageName>/.<adminReceiver>
//    adb shell dpm set-device-owner com.weguard.turboengines/.TEAdminReceiver
//    adb shell dpm remove-active-admin com.weguard.turboengines/.TEAdminReceiver

    val context = LocalContext.current
    val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//  val buttonState = remember { mutableStateOf(true) }
    val bottomSheetStatus = remember {
        mutableStateOf(false)
    }
    lateinit var timer: CountDownTimer
    val lockTaskButtonText = remember { mutableStateOf("Enable Lock Task Mode") }
    val lockTaskStatus = remember { mutableStateOf(false) }
    val lockDeviceStatus = remember { mutableStateOf(false) }
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
    timer = object : CountDownTimer(600000, 120000) {
        override fun onTick(millisUntilFinished: Long) {
            devicePolicyManager.setNetworkLoggingEnabled(componentName, true)
            Log.d(
                "NetworkLogStatus",
                devicePolicyManager.isNetworkLoggingEnabled(componentName).toString()
            )
            val networkLogs = devicePolicyManager.retrieveNetworkLogs(componentName, 0)
            Log.d("NetworkLogStatus", "${networkLogs.toString()} ${millisUntilFinished / 60000}")
            if (networkLogs != null) {
                for (log in networkLogs) {
                    Log.d("NetworkLogStatus", log.toString())
                }
            } else {
                Log.d("NetworkLogStatus", "No logs")
            }
        }

        override fun onFinish() {
            timer.cancel()
        }

    }
    timer.start()

    if (devicePolicyManager.isAdminActive(componentName)) {
        Log.d("AdminAvailableNActive", "Admin is active")
        Log.d(
            "Admin_pendingSystemUpdate",
            "${devicePolicyManager.getPendingSystemUpdate(componentName)}"
        )
//        Log.d("Device_OS", Build.DEVICE)
//        Log.d("Device_ID", Build.ID)
//        Log.d("Device_SKU",Build.SKU)
    } else {
        Log.d("AdminAvailableNActive", "Admin is not active")
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED, componentName)
        context.startActivity(intent)
    }

    if(devicePolicyManager.isDeviceOwnerApp("com.weguard.turboengines")){
        Log.d("DPMState","Allowed")
    }else{
        Log.d("DPMState","Not allowed")
    }


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
                TopAppBarMode(
                    devicePolicyManager,
                    context,
                    lockTaskStatus,
                    contextAsComponentActivity,
                    lockTaskButtonText,
                    lockDeviceStatus,
                    componentName,
                    navController,
                    telephony
                )
            }
        },
        bottomBar = {
            if (bottomBarState.value) {
                BottomAppBarMode(
                    bottomSheetStatus,
                    appDrawerButtonState,
                    homeLauncherButtonState,
                    topAppBarState,
                    bottomBarState,
                    context
                )
            }
        }
    )
    {
        Column(
            modifier = Modifier.combinedClickable(
                enabled = true,
                onClick = {
                    topAppBarState.value = false
                    bottomBarState.value = false
                    Toast
                        .makeText(
                            context,
                            "Disabling Top App and Bottom Bars",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                },
                onDoubleClick = {
                    topAppBarState.value = true
                    bottomBarState.value = true
                    Toast
                        .makeText(
                            context,
                            "Enabling Top App and Bottom Bars",
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            ),
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

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TopAppBarMode(
    devicePolicyManager: DevicePolicyManager,
    context: ComponentActivity,
    lockTaskStatus: MutableState<Boolean>,
    contextAsComponentActivity: ComponentActivity,
    lockTaskButtonText: MutableState<String>,
    lockDeviceStatus: MutableState<Boolean>,
    componentName: ComponentName,
    navController: NavController,
    telephony: TelephonyManager
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

            Spacer(modifier = Modifier.width(3.dp))

//          HomeLauncher(
//              filter,
//              buttonState,
//              context,
//              devicePolicyManager,
//              componentName,
//              custom
//            )

            LockDevice(lockDeviceStatus, contextAsComponentActivity, navController)

            Spacer(modifier = Modifier.width(3.dp))

            Reboot(devicePolicyManager, componentName)

//            Log.d("DeviceInfo_SKU", Build.SKU)
//            Log.d("DeviceInfo_ID", Build.ID)
//            Log.d("DeviceInfo_", Build.DEVICE)
//            Log.d("DeviceInfo_BASE_OS", Build.VERSION.BASE_OS)
//            Log.d("DeviceInfo_HOST", Build.HOST)
//            Log.d("DeviceInfo_Board", Build.BOARD)
//            Log.d("DeviceInfo_BootLoader", Build.BOOTLOADER)
//            Log.d("DeviceInfo_Brand", Build.BRAND)
//            Log.d("DeviceInfo_Display", Build.DISPLAY)
//            Log.d("DeviceInfo_FingerPrint", Build.FINGERPRINT)
//            Log.d("DeviceInfo_Hardware", Build.HARDWARE)
//            Log.d("DeviceInfo_MFG", Build.MANUFACTURER)
////    Log.d("DeviceInfo_ODM_SKU", Build.ODM_SKU)
//            Log.d("DeviceInfo_Product", Build.PRODUCT)
////   java.lang.NoSuchFieldError: No field SOC_MODEL of type Ljava/lang/String; in class Landroid/os/Build; or its superclasses (declaration of 'android.os.Build' appears in /system/framework/framework.jar!classes2.dex)
////    Log.d("DeviceInfo_SOC_MODEL", Build.SOC_MODEL)
////    Log.d("DeviceInfo_SOC_MFG", Build.SOC_MANUFACTURER)
//            Log.d("DeviceInfo_Tags", Build.TAGS)
//            Log.d("DeviceInfo_User", Build.USER)
//            Log.d("DeviceInfo_Get_Radio_Version", Build.getRadioVersion())
////    Log.d("DeviceInfo_Get_Serial", Build.getSerial())
//            Log.d("DeviceInfo_32_Bits", Build.SUPPORTED_32_BIT_ABIS.contentToString())
//            Log.d("DeviceInfo_64_Bits", Build.SUPPORTED_64_BIT_ABIS.contentToString())
//            Log.d("DeviceInfo_Time", Build.TIME.toString())
//            Log.d("DeviceInfo_Supported_ABIs", Build.SUPPORTED_ABIS.contentToString())
//            Log.d("DeviceInfo_Model", Build.MODEL)
//            val partitions = Build.getFingerprintedPartitions()[1]
//            Log.d("DeviceInfo_FingerPrint_Partitions", partitions.name)
////    Log.d("DeviceInfo_", Build.UNKNOWN)
//
//            Log.d("DeviceInfo_Telephony_networkCountryIso", telephony.networkCountryIso)
//            Log.d("DeviceInfo_Telephony_networkOperator", telephony.networkOperator)
////    java.lang.NoSuchMethodError: No virtual method getPrimaryImei()Ljava/lang/String
////    Log.d("DeviceInfo_Telephony_primaryImei", telephony.primaryImei)
//            Log.d("DeviceInfo_Telephony_simSerialNumber", telephony.simSerialNumber)
//            Log.d("DeviceInfo_Telephony_Network_Specifier", telephony.networkSpecifier)
//            telephony.manufacturerCode?.let { Log.d("DeviceInfo_Telephony_Mfg_Code", it) }
////            java.lang.NoSuchMethodError: No virtual method getManualNetworkSelectionPlmn()
////            Log.d(
////                "DeviceInfo_Telephony_Manual_Network_Selection_Plmn",
////                telephony.manualNetworkSelectionPlmn
////            )
////            Log.d(
////                "DeviceInfo_Telephony_Manual_Network_Selection_Plmn",
////                telephony.manualNetworkSelectionPlmn
////            )
//            Log.d("DeviceInfo_Telephony_meid", telephony.meid)
//            Log.d("DeviceInfo_Telephony_mmsUAProfURL", telephony.mmsUAProfUrl)
//            Log.d("DeviceInfo_Telephony_mmsUserAgent", telephony.mmsUserAgent)
//            if (telephony.nai != null) {
//                Log.d("DeviceInfo_Telephony_nai", telephony.nai)
//            } else {
//                Log.d("DeviceInfo_Telephony_nai", "NA")
//            }
//            Log.d("DeviceInfo_Telephony_Network_Operator_Name", telephony.networkOperatorName)
//            Log.d("DeviceInfo_Telephony_simCountryISO", telephony.simCountryIso)
//            Log.d("DeviceInfo_Telephony_sim_operator", telephony.simOperatorName)
//            Log.d("DeviceInfo_Telephony_simOperator", telephony.simOperator)
//            Log.d("DeviceInfo_Telephony_subscriberID", telephony.subscriberId)
//            Log.d("DeviceInfo_Settings_Secure_ANDROID_ID", Settings.Secure.ANDROID_ID)
//            Log.d("DeviceInfo_Settings_Secure_Name", Settings.Secure.NAME)
//            Log.d("DeviceInfo_Settings_Secure_Default_Input_Method", Settings.Secure.DEFAULT_INPUT_METHOD)
//            Log.d("DeviceInfo_Settings_Secure_Accessibility_Enabled", Settings.Secure.ACCESSIBILITY_ENABLED)
//            Log.d("DeviceInfo_Settings_Secure_Enabled_InputMethods", Settings.Secure.ENABLED_INPUT_METHODS)
//            Log.d("DeviceInfo_Settings_Show_Settings_APN", Settings.ACTION_SETTINGS)
//            Log.d("DeviceInfo_Settings_Show_NFC", Settings.ACTION_DEVICE_INFO_SETTINGS)
//            Log.d("DeviceInfo_Settings_Show_Settings_For_DataSaver", Settings.ACTION_ADD_ACCOUNT)
//            telephony.typeAllocationCode?.let {
//                Log.d(
//                    "DeviceInfo_Telephony_Type_Allocation_Code",
//                    it
//                )
//            }
////            Log.d(
////                "DeviceInfo_Telephony_getIccAuthentication", telephony.getIccAuthentication(
////                    1,
////                    TelephonyManager.AUTHTYPE_EAP_SIM,
////                    1.toString()
////                )
////            )
//            telephony.getManufacturerCode(1)
//                ?.let { Log.d("DeviceInfo_Telephony_Manufacturer_Code@1", it) }
//            Log.d("DeviceInfo_Telephony_getMeid@1", telephony.getMeid(1))
////            java.lang.NoSuchMethodError: No virtual method getActiveModemCount()
////            Log.d("DeviceInfo_Telephony_ActiveModemCount", telephony.activeModemCount.toString())
////            java.lang.NoSuchMethodError: No virtual method getCallComposerStatus()
////            Log.d("DeviceInfo_Telephony_Call_Composer_Status", telephony.callComposerStatus.toString())
//            Log.d(
//                "DeviceInfo_Telephony_cardID_ForDefault_Euicc",
//                telephony.cardIdForDefaultEuicc.toString()
//            )
//            Log.d(
//                "DeviceInfo_Telephony_CarrierID_From_Sim_MccMnc",
//                telephony.carrierIdFromSimMccMnc.toString()
//            )
//            Log.d(
//                "DeviceInfo_Telephony_Data_Activity",
//                telephony.dataActivity.toString()
//            )
//            Log.d("DeviceInfo_Telephony_Data_State", telephony.dataState.toString())
//            Log.d(
//                "DeviceInfo_Telephony_Is_Concurrent_VoiceAndData_Supported",
//                telephony.isConcurrentVoiceAndDataSupported.toString()
//            )
////            java.lang.NoSuchMethodError: No virtual method isDataCapable()
////            Log.d("DeviceInfo_Telephony_Is_Data_Capable", telephony.isDataCapable.toString())
//            Log.d(
//                "DeviceInfo_Telephony_Is_HearingAid_Compatibility_Supported",
//                telephony.isHearingAidCompatibilitySupported.toString()
//            )
////            java.lang.NoSuchMethodError: No virtual method isManualNetworkSelectionAllowed()
////            Log.d(
////                "DeviceInfo_Telephony_Is_Manual_NetworkSelection_Allowed",
////                telephony.isManualNetworkSelectionAllowed.toString()
////            )
//            Log.d("DeviceInfo_Telephony_Is_Network_Roaming", telephony.isNetworkRoaming.toString())
//            Log.d("DeviceInfo_Telephony_Is_Rtt_Supported", telephony.isRttSupported.toString())
//            Log.d("DeviceInfo_Telephony_Is_SMS_Capable", telephony.isSmsCapable.toString())
//            Log.d("DeviceInfo_Telephony_Is_Voice_Capable", telephony.isVoiceCapable.toString())
//            Log.d("DeviceInfo_Telephony_Is_World_Phone", telephony.isWorldPhone.toString())
////            java.lang.NoSuchMethodError: No virtual method getNetworkSelectionMode()
////            Log.d("DeviceInfo_Telephony_Network_Selection_Mode", telephony.networkSelectionMode.toString())
////            java.lang.NoSuchMethodError: No virtual method getPhoneAccountHandle()
////            Log.d("DeviceInfo_Telephony_Phone_Account_Handle", telephony.phoneAccountHandle.toString())
//            Log.d("DeviceInfo_Telephony_Phone_Type", telephony.phoneType.toString())
//            Log.d("DeviceInfo_Telephony_Sim_Carrier_Id", telephony.simCarrierId.toString())
//            Log.d("DeviceInfo_Telephony_Sim_CarrierId_Name", telephony.simCarrierIdName.toString())
//            Log.d(
//                "DeviceInfo_Telephony_Sim_Specific_CarrierId",
//                telephony.simSpecificCarrierId.toString()
//            )
//            Log.d(
//                "DeviceInfo_Telephony_Sim_Specific_CarrierId_Name",
//                telephony.simSpecificCarrierIdName.toString()
//            )
//            Log.d("DeviceInfo_Telephony_Sim_State", telephony.simState.toString())
////            java.lang.NoSuchMethodError: No virtual method getSubscriptionId()
////            Log.d("DeviceInfo_Telephony_Subscription_Id", telephony.subscriptionId.toString())
////            java.lang.NoSuchMethodError: No virtual method getSupportedModemCount()
////            Log.d("DeviceInfo_Telephony_Supported_Modem_Count", telephony.supportedModemCount.toString())
//            Log.d(
//                "DeviceInfo_Telephony_Supported_Radio_Access_Family",
//                telephony.supportedRadioAccessFamily.toString()
//            )
////            java.lang.SecurityException: Caller does not have permission. Though 'READ_PHONE_STATE and 'READ_PRIVELAGE_PHONE_STATE' permission are provided
////            Log.d("DeviceInfo_Telephony_Uicc_Cards_Info", telephony.uiccCardsInfo.toString())
////    Log.d("DeviceInfo_Telephony_Clear_Signal_Strength_Update_Request",
////        telephony.clearSignalStrengthUpdateRequest().toString()
////    )
        }
    )
}

@Composable
private fun LockDevice(
    lockDeviceStatus: MutableState<Boolean>,
    contextAsComponentActivity: ComponentActivity,
    navController: NavController
) {
    Button(onClick = {
        lockDeviceStatus.value = !(lockDeviceStatus.value)
    }
    ) {

        if (lockDeviceStatus.value) {
            Icon(Icons.TwoTone.Lock, contentDescription = "Lock the Device")
            // devicePolicyManager.lockNow()
            // Will do a new screen where the app is locked with lock task mode.
            contextAsComponentActivity.startLockTask()
            navController.navigate(route = "AdminLockScreen")
        } else {
            Icon(painterResource(id = R.drawable.unlock), contentDescription = "Unlock the device")
        }
    }
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
                Utils.setLockStatusValue(1)
                lockTaskButtonText.value = "Exit Lock Task Mode"
            }

            false -> {
                contextAsComponentActivity.stopLockTask()
                Utils.setLockStatusValue(0)
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
        color = Color.Black,
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
private fun BottomAppBarMode(
    bottomSheetStatus: MutableState<Boolean>,
    appDrawerButtonState: MutableState<Boolean>,
    homeLauncherButtonState: MutableState<Boolean>,
    topAppBarState: MutableState<Boolean>,
    bottomBarState: MutableState<Boolean>,
    context: ComponentActivity,
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
            IconButton(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission to be prompted
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        100
                    )
                } else {
                    Toast.makeText(context, "Permission already granted", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_question_mark_24),
                    contentDescription = "Phone permission"
                )
            }
        }
    }
}

@Composable
private fun Reboot(
    devicePolicyManager: DevicePolicyManager,
    componentName: ComponentName
) {
    Button(
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