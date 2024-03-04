package com.weguard.turboengines

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Half
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var gyroscope: Sensor? = null
    private var accelerometer: Sensor? = null
    private val linearAcceleration = FloatArray(3)
    private val gravity = FloatArray(3)
    private var callStatus: Boolean = false
    private val filter = IntentFilter()
    private val deviceLockStateReceiver = DeviceLockStateReceiver()
    private val contextAsComponentActivity = this@MainActivity as ComponentActivity
    private val callerLaunch =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            Activity.RESULT_CANCELED => result.resultCode = 0
            if (result.resultCode == Activity.RESULT_CANCELED) {
                contextAsComponentActivity.startLockTask()
            }
        }


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
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (gyroscope != null) {
            Toast.makeText(
                this@MainActivity,
                "Gyroscope and Accelerometer sensors exists...!",
                Toast.LENGTH_SHORT
            )
                .show()
        } else {
            Toast.makeText(
                this@MainActivity,
                "Gyroscope and Accelerometer sensor doesn't exists...!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onStart() {
        filter.addAction(Intent.ACTION_USER_UNLOCKED)
        this.registerReceiver(deviceLockStateReceiver, filter)
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        gyroscope?.let { gyroscopeSensor ->
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelerometer?.let { accelerometerSensor ->
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, gyroscope)
        sensorManager.unregisterListener(this, accelerometer)
    }

    override fun onStop() {
        unregisterReceiver(deviceLockStateReceiver)
        super.onStop()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // This time stamp delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (event != null) {

            when (event.sensor) {

                gyroscope -> {
                    // Axis of the rotation sample, not normalized yet.
                    var axisX: Float = event.values[0]
                    Log.d("Axis-X", "$axisX")
                    var axisY: Float = event.values[1]
                    Log.d("Axis-Y", "$axisY")
                    var axisZ: Float = event.values[2]
                    Log.d("Axis-Z", "$axisZ")

//                 Calculate the angular speed of the sample
//                 Angular velocity magnitude refers to the overall rate of rotation of an object around an axis,
//                 regardless of the direction of rotation
                    val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)
                    Log.d("omegaMagnitude", "$omegaMagnitude")

                    // Normalize the rotation vector if it's big enough to get the axis
                    // (i.e., EPSILON should represent your maximum allowable margin of error)
                    if (omegaMagnitude > Half.EPSILON) {
                        axisX /= omegaMagnitude
                        axisY /= omegaMagnitude
                        axisZ /= omegaMagnitude
                    }

                    callStatus = if (omegaMagnitude > 1.8f) {
                        Utils.setGyroscopeDetection(1)
                        true
                    } else {
                        Utils.setGyroscopeDetection(0)
                        false
                    }
                }

                accelerometer -> {
//                  alpha is calculated as t / (t + dT)
                    val alpha = 0.8f
                    val fallThreshold = -1.8f
                    // Isolate the force of gravity with the low-pass filter.
                    gravity[0] = alpha * gravity[0] + ((1 - alpha) * event.values[0])
                    Log.d("gravity X-Axis", "${gravity[0]}")
                    gravity[1] = alpha * gravity[1] + ((1 - alpha) * event.values[1])
                    Log.d("gravity Y-Axis", "${gravity[1]}")
                    gravity[2] = alpha * gravity[2] + ((1 - alpha) * event.values[2])
                    Log.d("gravity Z-Axis", "${gravity[2]}")

                    // Remove the gravity contribution with the high-pass filter.
                    linearAcceleration[0] = event.values[0] - gravity[0]
                    Log.d("Acceleration X-Axis", "${linearAcceleration[0]}")
                    linearAcceleration[1] = event.values[1] - gravity[1]
                    Log.d("Acceleration Y-Axis", "${linearAcceleration[1]}")
                    linearAcceleration[2] = event.values[2] - gravity[2]
                    Log.d("Acceleration Z-Axis", "${linearAcceleration[2]}")

                    if (linearAcceleration[1] < fallThreshold) {
                        Log.d("linearAcceleration", "${linearAcceleration[2]}")
                        Utils.setAccelerometerDetection(1)
                        callStatus = true
                    } else {
                        Log.d("linearAcceleration", "Safe....!")
                        Utils.setAccelerometerDetection(0)
                    }
                }
            }
        } else {
            Toast.makeText(this@MainActivity, "Empty..!", Toast.LENGTH_SHORT).show()
        }

        if (Utils.getGyroscopeDetection().intValue == 1 || Utils.getAccelerometerDetection().intValue == 1) {
            Log.d("Crash Detection", "Crash....!")
            Utils.setAccelerometerDetection(0)
            Utils.setGyroscopeDetection(0)
            if (callStatus && Utils.getLockStatusValue().intValue == 1) {
                contextAsComponentActivity.stopLockTask()
                val intent = Intent(Intent.ACTION_CALL)
                val phoneNumber = 1234567890
                intent.data = Uri.parse("tel: $phoneNumber")
                callerLaunch.launch(intent)
                callStatus = false
            } else if (callStatus) {
                val intent = Intent(Intent.ACTION_CALL)
                val phoneNumber = 1234567890
                intent.data = Uri.parse("tel: $phoneNumber")
                startActivity(intent)
                callStatus = false
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Toast.makeText(this@MainActivity, "Accuracy: $accuracy", Toast.LENGTH_SHORT).show()
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