package de.quartettmobile.companionapppoc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e("DEBUG", "FG SERVICE START COMMAND")

        showNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showNotification() {
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My Device is near")
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentText("Woah, my devices (${presentDevices.joinToString(", ")}) are near 😍")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()


        startForeground(
            NOTIFICATION_ID, notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        private val presentDevices = mutableSetOf<String>()

        fun foundDevice(context: Context, address: String) {
            presentDevices += address
            val intent = Intent(context, ForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun lostDevice(context: Context, address: String) {
            presentDevices -= address

            val intent = Intent(context, ForegroundService::class.java)
            context.stopService(intent)
        }

        const val CHANNEL_ID = "SomeChannelID"
        const val NOTIFICATION_ID = 12416
    }
}