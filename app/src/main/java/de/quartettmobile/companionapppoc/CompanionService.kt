package de.quartettmobile.companionapppoc

import android.annotation.SuppressLint
import android.companion.CompanionDeviceService
import android.util.Log


@SuppressLint("NewApi")
class CompanionService : CompanionDeviceService() {

    override fun onCreate() {
        super.onCreate()
        Log.e("DEBUG", "SERVICE CREATED")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("DEBUG", "SERVICE DESTROYED")
    }

    override fun onDeviceAppeared(deviceID: String) {
        Log.e("DEBUG", "DEVICE APPEARED $deviceID")
        ForegroundService.foundDevice(this, deviceID)
    }

    override fun onDeviceDisappeared(deviceID: String) {
        Log.e("DEBUG", "DEVICE DISAPPEARED $deviceID")
        ForegroundService.lostDevice(this, deviceID)
    }

}