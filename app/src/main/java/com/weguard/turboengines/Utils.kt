package com.weguard.turboengines

import androidx.compose.runtime.mutableIntStateOf

object Utils {
    private var gyroscopeCrashDetect = mutableIntStateOf(0)
    private var accelerometerCrashDetection = mutableIntStateOf(0)
    private var lockStatusValue = mutableIntStateOf(0)

    fun setGyroscopeDetection(crashValue: Int) {
        gyroscopeCrashDetect.intValue = crashValue
    }

    fun getGyroscopeDetection() = gyroscopeCrashDetect

    fun setAccelerometerDetection(crashAccValue: Int) {
        accelerometerCrashDetection.intValue = crashAccValue
    }

    fun getAccelerometerDetection() = accelerometerCrashDetection

    fun setLockStatusValue(lockValue: Int) {
        lockStatusValue.intValue = lockValue
    }

    fun getLockStatusValue() = lockStatusValue

}