package com.example.healthcare.funcation

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.healthcare.R
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class Massage : FirebaseMessagingService() {
    // get Token of device
    fun getToken(callback: (String) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                val token: String?
                if (it.isSuccessful) {
                    token = it.result
                    callback(token ?: "")
                }
            }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
        val body = message.notification?.body
        val channelID = "MassageFromApp"
        val notificationSoundUri = Uri.parse("android.resource://" + packageName + "/" + R.raw.sound_me)
        if (Build.VERSION.SDK_INT >= 26) {
            val channel =
                NotificationChannel(channelID, "Massage", NotificationManager.IMPORTANCE_HIGH)
            val s = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            s.createNotificationChannel(channel)
            val notification = Notification.Builder(this, channelID)
                .setContentText(body)
                .setContentTitle(title)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setAutoCancel(true)
                .setColor(Color.GREEN)
                .setSound(notificationSoundUri)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            NotificationManagerCompat.from(this).notify(5, notification.build())
        }else {
            val builder = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setColor(Color.GREEN)
                .setSound(notificationSoundUri)

            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(5, builder.build())

        }

        super.onMessageReceived(message)
    }

    fun subscribeTopic(topic : String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
    }

}