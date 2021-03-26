package de.quartettmobile.companionapppoc

import android.bluetooth.le.ScanFilter
import android.companion.*
import android.content.Intent
import android.content.IntentSender
import android.content.IntentSender.SendIntentException
import android.net.MacAddress
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.quartettmobile.companionapppoc.ui.theme.CompanionAppPOCTheme
import java.util.*


class MainActivity : AppCompatActivity() {

    private val companionDeviceManager: CompanionDeviceManager
        get() = getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

    @RequiresApi(31)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(
            "MainActivity",
            "Current Associations: ${companionDeviceManager.associations.joinToString(", ")}"
        )

        requestPermissions(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.REQUEST_OBSERVE_COMPANION_DEVICE_PRESENCE,
                android.Manifest.permission.BIND_COMPANION_DEVICE_SERVICE,
                android.Manifest.permission.ACCESS_WIFI_STATE,
            ), 0
        )

        setContent {
            CompanionAppPOCTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Association(this)
                }
            }
        }
    }

    @RequiresApi(31)
    @Suppress("DEPRECATION")
    fun associateWifi() {
        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        val bssid = wifiManager.connectionInfo.bssid

        Log.e("Callback", "BSSID! $bssid")

        val deviceFilter = WifiDeviceFilter.Builder()
            .setBssid(MacAddress.fromString(bssid))
            .build()

        val request = AssociationRequest.Builder()
            .setSingleDevice(true)
            .addDeviceFilter(deviceFilter)
            .build()

        val companionDeviceManager =
            getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        val value = object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(intentSender: IntentSender?) {
                Log.e("Callback", "Found! $intentSender")
                try {
                    startIntentSenderForResult(
                        intentSender,
                        SELECT_DEVICE_REQUEST_CODE_WIFI, null, 0, 0, 0
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(p0: CharSequence?) {
                Log.e("Callback", "Failed! $p0")
            }

        }

        companionDeviceManager.associate(request, value, Handler())
    }

    @RequiresApi(31)
    @Suppress("DEPRECATION")
    fun associateBluetooth() {
        val deviceAddress = "E0:2A:D7:3F:F2:4C"

        val scanFilter = ScanFilter.Builder()
            .setDeviceAddress(deviceAddress)
            .build()

        val deviceFilter = BluetoothLeDeviceFilter.Builder()
            .setScanFilter(scanFilter)
            .build()

        val request = AssociationRequest.Builder()
            .setSingleDevice(true)
            .addDeviceFilter(deviceFilter)
            .build()

        val companionDeviceManager =
            getSystemService(COMPANION_DEVICE_SERVICE) as CompanionDeviceManager

        val value = object : CompanionDeviceManager.Callback() {
            override fun onDeviceFound(intentSender: IntentSender?) {
                Log.e("Callback", "Found! $intentSender")
                try {
                    startIntentSenderForResult(
                        intentSender,
                        SELECT_DEVICE_REQUEST_CODE_BLE, null, 0, 0, 0
                    )
                } catch (e: SendIntentException) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(p0: CharSequence?) {
                Log.e("Callback", "Failed! $p0")
            }

        }

        companionDeviceManager.associate(request, value, Handler())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_DEVICE_REQUEST_CODE_WIFI) {
            val scanResult = data?.extras?.get("android.companion.extra.DEVICE") as? ScanResult
            Log.e(
                "MainActivity",
                "requestCode='$requestCode', resultCode='$resultCode', scanResult='${scanResult}'"
            )

            if (scanResult != null) {
                companionDeviceManager.startObservingDevicePresence(scanResult.BSSID)

                Log.e(
                    "MainActivity",
                    "Current Associations: ${companionDeviceManager.associations.joinToString(", ")}"
                )
            } else {
                Log.e(
                    "MainActivity",
                    "ScanResult is null"
                )
            }
        } else {
            Log.e(
                "MainActivity",
                "requestCode='$requestCode', resultCode='$resultCode', scanResult='${data?.extras?.keySet()}'"
            )
            val bleScanResult =
                data?.extras?.get("android.companion.extra.DEVICE") as? android.bluetooth.le.ScanResult
            val deviceAddress = bleScanResult?.device?.address
            if (deviceAddress != null) {
                companionDeviceManager.startObservingDevicePresence(deviceAddress)

                Log.e(
                    "MainActivity",
                    "Current Associations: ${companionDeviceManager.associations.joinToString(", ")}"
                )
            }
        }
    }

    fun disassociate() {
        companionDeviceManager.associations.forEach {
            companionDeviceManager.stopObservingDevicePresence(it)
            companionDeviceManager.disassociate(it)
        }
    }

    companion object {
        const val SELECT_DEVICE_REQUEST_CODE_WIFI = 32
        const val SELECT_DEVICE_REQUEST_CODE_BLE = 64
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun Association(mainActivity: MainActivity) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Button(onClick = { mainActivity.associateBluetooth() }) {
            Text(text = "Associate Nearby BLE Beacon")
        }
        Spacer(modifier = Modifier.size(50.dp))
        Button(onClick = { mainActivity.associateWifi() }) {
            Text(text = "Associate Current Network")
        }
        Spacer(modifier = Modifier.size(50.dp))
        Button(onClick = { mainActivity.disassociate() }) {
            Text(text = "DisassociateAll")
        }
    }
}
