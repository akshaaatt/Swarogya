package com.aemerse.svarogya.notifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.aemerse.svarogya.R
import com.aemerse.svarogya.home.HomeActivity
import com.aemerse.svarogya.starting.Splash

class NotificationHelper(base: Context?, healthReportIcon: Bitmap, requestCode: Int) : ContextWrapper(base) {

    private val healthIcon = healthReportIcon
    private var mManager: NotificationManager? = null
    private lateinit var beforeTime: String
    private var intentActivity: Class<*>

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
        manager!!.createNotificationChannel(channel)
    }

    val manager: NotificationManager? get() {
        if (mManager == null) {
            mManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return mManager
    }

    val channelNotification: NotificationCompat.Builder get() = NotificationCompat.Builder(applicationContext, channelID)
        .setContentTitle("Reminder!")
        .setContentText("Update medical records before $beforeTime")
        .setSmallIcon(R.drawable.ic_round_local_hospital_24)
        .setColor(ContextCompat.getColor(this,R.color.blue_diff))
        .setLargeIcon(healthIcon)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setTimeoutAfter(1800000)
        .setAutoCancel(true)
        .setContentIntent(PendingIntent.getActivity(baseContext, 0, Intent(baseContext, intentActivity), 0))

    companion object {
        const val channelID = "phoneId"
        const val channelName = "phoneChannel"
    }

    init {
        when (requestCode) {
            0 -> {
                beforeTime = "12:30 AM"
            }
            1 -> {
                beforeTime = "6:30 AM"
            }
            2 -> {
                beforeTime = "12:30 PM"
            }
            3 -> {
                beforeTime = "6:30 PM"
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
        intentActivity = if(FirebaseAuth.getInstance().currentUser!=null) {
            HomeActivity::class.java
        } else{
            Splash::class.java
        }
    }
}