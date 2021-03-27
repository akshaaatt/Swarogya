package com.swarogya.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.swarogya.app.R

class AlertReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Glide.with(context).asBitmap().load(R.drawable.ic_diagnosis_24dp).into(object : CustomTarget<Bitmap>() {
            override fun onLoadCleared(placeholder: Drawable?) {}
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val notificationHelper = NotificationHelper(context,resource,intent.extras!!.getInt("requestCode"))
                val nb = notificationHelper.channelNotification
                notificationHelper.manager?.notify(1, nb.build())
            }
        })
    }
}